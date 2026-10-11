package com.example.saga.payment.domain.payment.vo

import java.util.UUID

@JvmInline
value class PaymentId private constructor(
    val value: UUID
) {

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: UUID): PaymentId = PaymentId(value)
    }
}
