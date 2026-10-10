package com.example.saga.platform.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface OutboxShardJpaRepository : JpaRepository<OutboxShardEntity, Int> {

    @Transactional
    @Modifying
    @Query(
        value = "update outbox_shards " +
            "set owner = :owner, lease_until = timestampadd(microsecond, :leaseMicros, now(6)) " +
            "where shard = :shard and (lease_until is null or lease_until < now(6))",
        nativeQuery = true,
    )
    fun acquireLease(shard: Int, owner: String, leaseMicros: Long): Int

    @Transactional
    @Modifying
    @Query(
        value = "update outbox_shards " +
            "set lease_until = timestampadd(microsecond, :leaseMicros, now(6)) " +
            "where shard = :shard and owner = :owner",
        nativeQuery = true,
    )
    fun renewLease(shard: Int, owner: String, leaseMicros: Long): Int

    @Transactional
    @Modifying
    @Query(
        value = "update outbox_shards set owner = null, lease_until = null where shard = :shard and owner = :owner",
        nativeQuery = true,
    )
    fun releaseLease(shard: Int, owner: String): Int
}
