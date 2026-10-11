package com.example.saga.payment.contract.command

data class PayOrder(val orderId: String, val amount: Long)
data class CancelOrderPayment(val orderId: String)
