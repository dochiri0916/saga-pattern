package com.example.saga.payment.application.port.`in`.result

import com.example.saga.payment.domain.Payment

data class GetPaymentResult(
    val paymentId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
) {
    companion object {
        fun from(payment: Payment): GetPaymentResult = GetPaymentResult(
            paymentId = payment.id.toString(),
            orderId = payment.orderId.toString(),
            amount = payment.amount.value,
            status = payment.status.name,
        )
    }
}
