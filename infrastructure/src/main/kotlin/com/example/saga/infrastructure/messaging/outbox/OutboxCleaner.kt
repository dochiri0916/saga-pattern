package com.example.saga.infrastructure.messaging.outbox

import java.time.Clock
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Limit
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

private val log = LoggerFactory.getLogger(OutboxCleaner::class.java)

class OutboxCleaner(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val shardLease: OutboxShardLease,
    private val properties: OutboxProperties,
    private val clock: Clock,
    transactionManager: PlatformTransactionManager,
) {

    private val batchTransaction = TransactionTemplate(transactionManager).apply {
        isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED
    }

    @Scheduled(fixedDelayString = "\${saga.outbox.cleanup.interval}")
    fun clean() {
        val cutoff = clock.instant().minus(properties.cleanup.retention)
        var deleted = 0

        for (shard in 0 until properties.shardCount) {
            do {
                val batch = deleteBatchWithLease(shard, cutoff)
                deleted += batch.deleted
            } while (batch.mayHaveMore(properties.cleanup.batchSize))
        }
        if (deleted > 0) log.info("보관 기간이 지난 아웃박스 행을 삭제했습니다. deleted={}, cutoff={}", deleted, cutoff)
    }

    private fun deleteBatchWithLease(shard: Int, cutoff: Instant): CleanupBatch {
        if (!shardLease.tryAcquire(shard, WORKER)) return CleanupBatch(fetched = 0, deleted = 0)
        try {
            return batchTransaction.execute { deleteBatch(shard, cutoff) }
        } finally {
            shardLease.release(shard, WORKER)
        }
    }

    private fun deleteBatch(shard: Int, cutoff: Instant): CleanupBatch {
        val oldest = outboxJpaRepository
            .findByShardAndStatusOrderByIdAsc(shard, OutboxStatus.PUBLISHED, Limit.of(properties.cleanup.batchSize))
        val expired = oldest.filter { it.publishedAt?.isBefore(cutoff) == true }
        if (expired.isNotEmpty()) outboxJpaRepository.deleteAllInBatch(expired)

        return CleanupBatch(fetched = oldest.size, deleted = expired.size)
    }

    private data class CleanupBatch(val fetched: Int, val deleted: Int) {

        fun mayHaveMore(batchSize: Int): Boolean = fetched == batchSize && deleted > 0
    }

    private companion object {
        const val WORKER = "outbox-cleaner"
    }
}
