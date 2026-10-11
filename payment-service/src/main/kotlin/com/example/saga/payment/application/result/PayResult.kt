package com.example.saga.payment.application.result

import com.example.saga.payment.domain.payment.Payment

data class PayResult(
    val paymentId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
) {
    companion object {
        fun from(payment: Payment): PayResult = PayResult(
            paymentId = payment.id.toString(),
            orderId = payment.orderId.toString(),
            amount = payment.amount.value,
            status = payment.status.name,
        )
    }
}
