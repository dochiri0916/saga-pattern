package com.example.saga.payment.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "order_payment_cancellations",
    indexes = [Index(name = "idx_order_payment_cancellations_cancelled_at", columnList = "cancelled_at")],
)
final class OrderPaymentCancellationEntity private constructor(
    orderId: UUID,
    cancelledAt: Instant,
) {

    @field:Id
    @field:Column(name = "order_id", nullable = false, updatable = false)
    final var orderId: UUID = orderId
        private set

    @field:Column(name = "cancelled_at", nullable = false, updatable = false)
    final var cancelledAt: Instant = cancelledAt
        private set
}
