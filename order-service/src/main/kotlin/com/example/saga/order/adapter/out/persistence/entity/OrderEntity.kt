package com.example.saga.order.adapter.out.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID

@Entity
@Table(name = "orders")
final class OrderEntity private constructor(
    publicId: UUID,
    status: String,
    items: List<OrderItemEntity>,
) {

    init {
        items.forEach { it.order = this }
    }

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

    @field:Column(name = "status", nullable = false)
    final var status: String = status
        private set

    @field:OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    final val items: MutableList<OrderItemEntity> = items.toMutableList()

    fun updateStatus(status: String) {
        this.status = status
    }

    companion object {
        fun of(publicId: UUID, status: String, items: List<OrderItemEntity>): OrderEntity =
            OrderEntity(publicId, status, items)
    }
}
