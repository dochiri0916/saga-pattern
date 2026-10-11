package com.example.saga.order.domain.order.vo

import com.example.saga.order.domain.order.exception.InvalidOrderQuantityException

@JvmInline
value class OrderQuantity private constructor(
    val value: Int
) {

    init {
        if (value < 1) throw InvalidOrderQuantityException(value)
    }

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: Int): OrderQuantity = OrderQuantity(value)
    }
}
