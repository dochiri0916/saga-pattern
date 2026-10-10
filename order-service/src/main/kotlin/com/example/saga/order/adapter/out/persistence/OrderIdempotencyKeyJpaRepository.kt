package com.example.saga.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface OrderIdempotencyKeyJpaRepository : JpaRepository<OrderIdempotencyKeyEntity, String> {

    @Modifying
    @Query(
        value = "insert ignore into order_idempotency_keys (idempotency_key, order_id, request_hash, created_at) " +
            "values (:idempotencyKey, :orderId, :requestHash, :createdAt)",
        nativeQuery = true,
    )
    fun insertIgnore(idempotencyKey: String, orderId: UUID, requestHash: String, createdAt: Instant): Int

    @Transactional
    @Modifying
    @Query(value = "delete from order_idempotency_keys where created_at < :cutoff limit :batchSize", nativeQuery = true)
    fun deleteCreatedBefore(cutoff: Instant, batchSize: Int): Int
}
