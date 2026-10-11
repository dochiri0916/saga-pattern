package com.example.saga.infrastructure.messaging.outbox

import java.time.Instant
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OutboxJpaRepository : JpaRepository<OutboxJpaEntity, Long> {

    @Query("select distinct o.shard from OutboxJpaEntity o where o.status = :status")
    fun findShardsByStatus(status: OutboxStatus): List<Int>

    fun findByShardAndStatusOrderByIdAsc(shard: Int, status: OutboxStatus, limit: Limit): List<OutboxJpaEntity>

    fun findFirstByShardAndStatusOrderByIdAsc(shard: Int, status: OutboxStatus): OutboxJpaEntity?

    fun countByShardInAndStatus(shards: Collection<Int>, status: OutboxStatus): Long

    @Modifying
    @Query(
        "update OutboxJpaEntity o " +
            "set o.status = com.example.saga.infrastructure.messaging.outbox.OutboxStatus.PUBLISHED, o.publishedAt = :publishedAt " +
            "where o.id in :ids"
    )
    fun markPublished(ids: List<Long>, publishedAt: Instant): Int
}
