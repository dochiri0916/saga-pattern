package com.example.saga.order.domain.vo

import com.example.saga.order.domain.exception.NegativeAmountException
import com.example.saga.order.domain.exception.OrderAmountOverflowException

@JvmInline
value class OrderAmount private constructor(
    val value: Long
) {

    init {
        if (value < 0) throw NegativeAmountException(NegativeAmountException.Subject.ORDER_AMOUNT, value)
    }

    operator fun plus(other: OrderAmount): OrderAmount {
        val sum = try {
            Math.addExact(value, other.value)
        } catch (e: ArithmeticException) {
            throw OrderAmountOverflowException(e)
        }
        return OrderAmount(sum)
    }

    override fun toString(): String = value.toString()

    companion object {
        val ZERO = OrderAmount(0L)

        fun of(value: Long): OrderAmount = OrderAmount(value)
    }
}
