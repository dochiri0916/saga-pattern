package com.example.saga.inventory.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID

@Entity
@Table(name = "stocks")
final class StockEntity private constructor(
    publicId: UUID,
    name: String,
    quantity: Int,
    price: Int,
) {

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set

    @field:Version
    @field:Column(name = "version", nullable = false)
    final var version: Long? = null
        private set

    @field:Column(name = "public_id", nullable = false, updatable = false, unique = true)
    final var publicId: UUID = publicId
        private set

    @field:Column(name = "name", nullable = false)
    final var name: String = name
        private set

    @field:Column(name = "quantity", nullable = false)
    final var quantity: Int = quantity
        private set

    @field:Column(name = "price", nullable = false)
    final var price: Int = price
        private set

    fun update(name: String, quantity: Int, price: Int) {
        this.name = name
        this.quantity = quantity
        this.price = price
    }

    companion object {
        fun of(publicId: UUID, name: String, quantity: Int, price: Int): StockEntity =
            StockEntity(publicId, name, quantity, price)
    }
}
