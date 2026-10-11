package com.example.saga.order.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "stock_snapshots")
final class StockSnapshotEntity private constructor(
    stockId: UUID,
    name: String,
    price: Int,
    quantity: Int,
    version: Long,
) {

    @field:Id
    @field:Column(name = "stock_id", nullable = false, updatable = false)
    final var stockId: UUID = stockId
        private set

    @field:Column(name = "name", nullable = false)
    final var name: String = name
        private set

    @field:Column(name = "price", nullable = false)
    final var price: Int = price
        private set

    @field:Column(name = "quantity", nullable = false)
    final var quantity: Int = quantity
        private set

    @field:Column(name = "version", nullable = false)
    final var version: Long = version
        private set

    fun update(name: String, price: Int, quantity: Int, version: Long) {
        this.name = name
        this.price = price
        this.quantity = quantity
        this.version = version
    }

    companion object {
        fun of(stockId: UUID, name: String, price: Int, quantity: Int, version: Long): StockSnapshotEntity =
            StockSnapshotEntity(stockId, name, price, quantity, version)
    }
}
