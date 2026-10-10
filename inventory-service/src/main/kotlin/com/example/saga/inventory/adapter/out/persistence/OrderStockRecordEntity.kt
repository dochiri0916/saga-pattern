package com.example.saga.inventory.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "order_stock_records",
    indexes = [Index(name = "idx_order_stock_records_updated_at", columnList = "updated_at")],
)
final class OrderStockRecordEntity private constructor(
    orderId: UUID,
    status: String,
    updatedAt: Instant,
) {

    @field:Id
    @field:Column(name = "order_id", nullable = false, updatable = false)
    final var orderId: UUID = orderId
        private set

    @field:Column(name = "status", nullable = false)
    final var status: String = status
        private set

    @field:Column(name = "updated_at", nullable = false)
    final var updatedAt: Instant = updatedAt
        private set

    fun update(status: String, updatedAt: Instant) {
        this.status = status
        this.updatedAt = updatedAt
    }

    companion object {
        fun of(orderId: UUID, status: String, updatedAt: Instant): OrderStockRecordEntity =
            OrderStockRecordEntity(orderId, status, updatedAt)
    }
}
