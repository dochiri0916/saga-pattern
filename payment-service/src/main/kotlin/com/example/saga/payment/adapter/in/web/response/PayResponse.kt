package com.example.saga.payment.adapter.`in`.web.response

import com.example.saga.payment.application.port.`in`.result.PayResult

data class PayResponse(
    val paymentId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
) {
    companion object {
        fun from(result: PayResult): PayResponse = PayResponse(
            paymentId = result.paymentId,
            orderId = result.orderId,
            amount = result.amount,
            status = result.status,
        )
    }
}
