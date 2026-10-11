package com.example.saga.shipping.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "shipments")
final class ShipmentEntity private constructor(
    publicId: UUID,
    orderId: UUID,
    status: String,
) {

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set

    @field:Column(name = "public_id", nullable = false, updatable = false, unique = true)
    final var publicId: UUID = publicId
        private set

    @field:Column(name = "order_id", nullable = false, updatable = false, unique = true)
    final var orderId: UUID = orderId
        private set

    @field:Column(name = "status", nullable = false)
    final var status: String = status
        private set

    fun updateStatus(status: String) {
        this.status = status
    }

    companion object {
        fun of(publicId: UUID, orderId: UUID, status: String): ShipmentEntity = ShipmentEntity(publicId, orderId, status)
    }
}
