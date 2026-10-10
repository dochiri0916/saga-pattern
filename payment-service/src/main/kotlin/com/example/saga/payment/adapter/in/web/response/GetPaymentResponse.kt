package com.example.saga.payment.adapter.`in`.web.response

import com.example.saga.payment.application.port.`in`.result.GetPaymentResult

data class GetPaymentResponse(
    val paymentId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
) {
    companion object {
        fun from(result: GetPaymentResult): GetPaymentResponse = GetPaymentResponse(
            paymentId = result.paymentId,
            orderId = result.orderId,
            amount = result.amount,
            status = result.status,
        )
    }
}
