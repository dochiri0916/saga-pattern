package com.example.saga.payment.adapter.`in`.web.response

data class PayResponse(
    val paymentId: String,
    val orderId: String,
    val amount: Int,
    val status: String,
)
