package com.example.saga.inventory.domain.orderstock.vo

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
