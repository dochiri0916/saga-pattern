package com.example.saga.order.adapter.`in`.web.response

data class OrderItemResponse(
    val itemId: String,
    val stockId: String,
    val quantity: Int,
    val unitPrice: Int,
    val amount: Long,
    val status: String,
)
