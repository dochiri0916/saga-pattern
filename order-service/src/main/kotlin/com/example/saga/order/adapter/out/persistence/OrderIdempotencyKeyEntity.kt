package com.example.saga.order.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "order_idempotency_keys",
    indexes = [Index(name = "idx_order_idempotency_keys_created_at", columnList = "created_at")],
)
final class OrderIdempotencyKeyEntity private constructor(
    idempotencyKey: String,
    orderId: UUID,
    requestHash: String,
    createdAt: Instant,
) {

    @field:Id
    @field:Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    final var idempotencyKey: String = idempotencyKey
        private set

    @field:Column(name = "order_id", nullable = false, updatable = false)
    final var orderId: UUID = orderId
        private set

    @field:Column(name = "request_hash", nullable = false, updatable = false, length = 64)
    final var requestHash: String = requestHash
        private set

    @field:Column(name = "created_at", nullable = false, updatable = false)
    final var createdAt: Instant = createdAt
        private set
}
