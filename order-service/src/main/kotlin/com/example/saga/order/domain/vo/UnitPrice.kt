package com.example.saga.order.domain.vo

import com.example.saga.order.domain.exception.NegativeAmountException

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
