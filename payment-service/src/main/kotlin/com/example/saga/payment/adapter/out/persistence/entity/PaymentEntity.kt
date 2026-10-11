package com.example.saga.payment.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID

@Entity
@Table(name = "payments")
final class PaymentEntity private constructor(
    publicId: UUID,
    orderId: UUID,
    amount: Int,
    status: String,
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

    @field:Column(name = "order_id", nullable = false, updatable = false, unique = true)
    final var orderId: UUID = orderId
        private set

    @field:Column(name = "amount", nullable = false)
    final var amount: Int = amount
        private set

    @field:Column(name = "status", nullable = false)
    final var status: String = status
        private set

    fun updateStatus(status: String) {
        this.status = status
    }

    companion object {
        fun of(publicId: UUID, orderId: UUID, amount: Int, status: String): PaymentEntity =
            PaymentEntity(publicId, orderId, amount, status)
    }
}
