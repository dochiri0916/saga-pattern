package com.example.saga.payment.domain.payment.vo

import com.example.saga.payment.domain.payment.exception.InvalidPaymentAmountException

@JvmInline
value class PaymentAmount private constructor(
    val value: Int
) {

    init {
        if (value < 1) throw InvalidPaymentAmountException(value)
    }

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: Int): PaymentAmount = PaymentAmount(value)
    }
}
