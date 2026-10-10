package com.example.saga.order.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "order_items")
final class OrderItemEntity private constructor(
    publicId: UUID,
    stockId: UUID,
    quantity: Int,
    unitPrice: Int,
    status: String,
) {

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set

    @field:Column(name = "public_id", nullable = false, updatable = false, unique = true)
    final var publicId: UUID = publicId
        private set

    @field:Column(name = "stock_id", nullable = false, updatable = false)
    final var stockId: UUID = stockId
        private set

    @field:Column(name = "quantity", nullable = false, updatable = false)
    final var quantity: Int = quantity
        private set

    @field:Column(name = "unit_price", nullable = false, updatable = false)
    final var unitPrice: Int = unitPrice
        private set

    @field:Column(name = "status", nullable = false)
    final var status: String = status
        private set

    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(name = "order_id", nullable = false, updatable = false)
    final var order: OrderEntity? = null
        internal set

    fun updateStatus(status: String) {
        this.status = status
    }

    companion object {
        fun of(publicId: UUID, stockId: UUID, quantity: Int, unitPrice: Int, status: String): OrderItemEntity =
            OrderItemEntity(publicId, stockId, quantity, unitPrice, status)
    }
}
