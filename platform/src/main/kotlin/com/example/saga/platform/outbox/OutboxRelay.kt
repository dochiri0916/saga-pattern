package com.example.saga.platform.outbox

import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.data.domain.Limit
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.Duration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger(OutboxRelay::class.java)

class OutboxRelay(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val outboxShardJpaRepository: OutboxShardJpaRepository,
    private val shardLease: OutboxShardLease,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val properties: OutboxProperties,
    private val clock: Clock,
    transactionManager: PlatformTransactionManager,
) : SmartLifecycle {

    private val transaction = TransactionTemplate(transactionManager)

    private val workers = List(properties.relay.concurrency) { Worker("outbox-relay-$it") }

    @Volatile
    private var running = false

    @PostConstruct
    fun verifyShards() {
        val shardRows = outboxShardJpaRepository.count()
        check(shardRows == properties.shardCount.toLong()) {
            "outbox_shards 행 수($shardRows)가 saga.outbox.shard-count(${properties.shardCount})와 다릅니다."
        }
        val misplaced = outboxJpaRepository.findShardsByStatus(OutboxStatus.PENDING)
            .flatMap { outboxJpaRepository.findByShardAndStatusOrderByIdAsc(it, OutboxStatus.PENDING, Limit.of(VERIFY_SAMPLE)) }
            .firstOrNull { it.shard != properties.shardOf(it.messageKey) }
        check(misplaced == null) {
            "발행 대기 중인 이벤트(outboxId=${misplaced?.id})의 shard가 현재 shard-count로 계산한 값과 다릅니다. " +
                "샤드 수는 PENDING을 모두 비운 뒤에 바꿔야 합니다."
        }
    }

    fun wake() {
        workers.forEach { it.wake() }
    }

    override fun start() {
        running = true
        workers.forEach { it.start() }
    }

    override fun stop() {
        running = false
        wake()
        val deadline = System.nanoTime() + STOP_TIMEOUT.toNanos()
        workers.forEach { it.awaitStop(deadline) }
    }

    override fun isRunning(): Boolean = running

    private fun relay(worker: String): Boolean {
        var skippedBusyShard = false
        for (shard in outboxJpaRepository.findShardsByStatus(OutboxStatus.PENDING).shuffled()) {
            if (!running) break
            if (!relayShard(shard, worker)) skippedBusyShard = true
        }
        return skippedBusyShard
    }

    private fun relayShard(shard: Int, worker: String): Boolean {
        if (!shardLease.tryAcquire(shard, worker)) return false
        try {
            while (running && publishBatch(shard) && shardLease.renew(shard, worker)) {

            }
        } finally {
            shardLease.release(shard, worker)
        }
        return true
    }

    private fun publishBatch(shard: Int): Boolean {
        val batchSize = properties.relay.batchSize
        val events = outboxJpaRepository.findByShardAndStatusOrderByIdAsc(shard, OutboxStatus.PENDING, Limit.of(batchSize))
        if (events.isEmpty()) return false

        try {
            kafkaTemplate.executeInTransaction { kafka -> events.forEach { kafka.send(it.toRecord()) } }
        } catch (e: Exception) {

            log.warn(
                "아웃박스 배치 발행에 실패했습니다. 다음 주기에 다시 시도합니다. shard={}, outboxId={}..{}, cause={}",
                shard, events.first().id, events.last().id, e.toString()
            )
            return false
        }
        transaction.executeWithoutResult { outboxJpaRepository.markPublished(events.mapNotNull { it.id }, clock.instant()) }

        return events.size == batchSize
    }

    private fun OutboxEntity.toRecord(): ProducerRecord<String, String> =
        ProducerRecord(topic, messageKey, payload).apply {
            headers().add(OutboxHeaders.EVENT_ID, eventId.toString().toByteArray())
            headers().add(OutboxHeaders.EVENT_TYPE, eventType.toByteArray())
        }

    private inner class Worker(private val name: String) {

        private val wakeSignal = ArrayBlockingQueue<Unit>(1)
        private val thread = Thread(::runLoop, name).apply { isDaemon = true }

        fun start() = thread.start()

        fun wake() {
            wakeSignal.offer(Unit)
        }

        fun awaitStop(deadlineNanos: Long) {
            thread.join(Duration.ofNanos((deadlineNanos - System.nanoTime()).coerceAtLeast(1)))
            if (thread.isAlive) thread.interrupt()
        }

        private fun runLoop() {
            var waitTime = Duration.ZERO
            while (running) {
                try {
                    wakeSignal.poll(waitTime.toMillis(), TimeUnit.MILLISECONDS)
                    if (!running) return
                    waitTime = if (relay(name)) BUSY_RETRY_DELAY else properties.relay.pollInterval
                } catch (e: InterruptedException) {
                    return
                } catch (e: Throwable) {

                    log.error("아웃박스 릴레이 실행 중 오류가 발생했습니다. 다음 주기에 다시 시도합니다. cause={}", e.toString())
                    waitTime = properties.relay.pollInterval
                }
            }
        }
    }

    private companion object {
        val BUSY_RETRY_DELAY: Duration = Duration.ofMillis(100)
        val STOP_TIMEOUT: Duration = Duration.ofSeconds(15)
        const val VERIFY_SAMPLE = 100
    }
}
