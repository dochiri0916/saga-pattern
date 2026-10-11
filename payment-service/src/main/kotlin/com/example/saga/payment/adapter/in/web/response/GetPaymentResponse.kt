package com.example.saga.payment.adapter.`in`.web.response

data class GetPaymentResponse(
    val paymentId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
)
