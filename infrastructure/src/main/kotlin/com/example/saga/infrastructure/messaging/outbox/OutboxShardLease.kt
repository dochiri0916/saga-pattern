package com.example.saga.infrastructure.messaging.outbox

import java.util.UUID
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(OutboxShardLease::class.java)

class OutboxShardLease(
    private val outboxShardJpaRepository: OutboxShardJpaRepository,
    private val properties: OutboxProperties,
) {

    private val instanceId = UUID.randomUUID().toString()

    private val leaseMicros = properties.relay.leaseDuration.toNanos() / 1_000

    fun tryAcquire(shard: Int, worker: String): Boolean =
        outboxShardJpaRepository.acquireLease(shard, ownerOf(worker), leaseMicros) == 1

    fun renew(shard: Int, worker: String): Boolean =
        outboxShardJpaRepository.renewLease(shard, ownerOf(worker), leaseMicros) == 1

    fun release(shard: Int, worker: String) {
        try {
            outboxShardJpaRepository.releaseLease(shard, ownerOf(worker))
        } catch (e: Exception) {
            log.warn("아웃박스 샤드 임대 반납에 실패했습니다. 임대 기간이 지나면 풀립니다. shard={}, cause={}", shard, e.toString())
        }
    }

    private fun ownerOf(worker: String) = "$instanceId:$worker"
}
