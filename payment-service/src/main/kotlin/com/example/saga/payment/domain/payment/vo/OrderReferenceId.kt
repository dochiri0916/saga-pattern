package com.example.saga.payment.domain.payment.vo

import java.util.UUID

@JvmInline
value class OrderReferenceId private constructor(
    val value: UUID
) {

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: UUID): OrderReferenceId = OrderReferenceId(value)
    }
}
