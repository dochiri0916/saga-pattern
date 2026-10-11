package com.example.saga.order.domain.order.vo

import com.example.saga.order.domain.order.exception.NegativeAmountException

@JvmInline
value class UnitPrice private constructor(
    val value: Int
) {

    init {
        if (value < 0) throw NegativeAmountException(NegativeAmountException.Subject.UNIT_PRICE, value.toLong())
    }

    operator fun times(quantity: OrderQuantity): OrderAmount = OrderAmount.of(value.toLong() * quantity.value)

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: Int): UnitPrice = UnitPrice(value)
    }
}
