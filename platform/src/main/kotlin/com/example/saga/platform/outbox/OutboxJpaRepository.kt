package com.example.saga.platform.outbox

import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface OutboxJpaRepository : JpaRepository<OutboxEntity, Long> {

    @Query("select distinct o.shard from OutboxEntity o where o.status = :status")
    fun findShardsByStatus(status: OutboxStatus): List<Int>

    fun findByShardAndStatusOrderByIdAsc(shard: Int, status: OutboxStatus, limit: Limit): List<OutboxEntity>

    fun findFirstByShardAndStatusOrderByIdAsc(shard: Int, status: OutboxStatus): OutboxEntity?

    fun countByShardInAndStatus(shards: Collection<Int>, status: OutboxStatus): Long

    @Modifying
    @Query(
        "update OutboxEntity o " +
            "set o.status = com.example.saga.platform.outbox.OutboxStatus.PUBLISHED, o.publishedAt = :publishedAt " +
            "where o.id in :ids"
    )
    fun markPublished(ids: List<Long>, publishedAt: Instant): Int
}
