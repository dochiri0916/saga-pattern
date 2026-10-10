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
    name = "order_sagas",
    indexes = [Index(name = "idx_order_sagas_status_updated_at", columnList = "status, updated_at")],
)
final class OrderSagaEntity private constructor(
    orderId: UUID,
    status: String,
    failureReason: String?,
    startedAt: Instant,
    updatedAt: Instant,
) {

    @field:Id
    @field:Column(name = "order_id", nullable = false, updatable = false)
    final var orderId: UUID = orderId
        private set

    @field:Column(name = "status", nullable = false)
    final var status: String = status
        private set

    @field:Column(name = "failure_reason")
    final var failureReason: String? = failureReason
        private set

    @field:Column(name = "started_at", nullable = false, updatable = false)
    final var startedAt: Instant = startedAt
        private set

    @field:Column(name = "updated_at", nullable = false)
    final var updatedAt: Instant = updatedAt
        private set

    fun update(status: String, failureReason: String?, updatedAt: Instant) {
        this.status = status
        this.failureReason = failureReason
        this.updatedAt = updatedAt
    }

    companion object {
        fun of(orderId: UUID, status: String, failureReason: String?, startedAt: Instant, updatedAt: Instant): OrderSagaEntity =
            OrderSagaEntity(orderId, status, failureReason, startedAt, updatedAt)
    }
}
