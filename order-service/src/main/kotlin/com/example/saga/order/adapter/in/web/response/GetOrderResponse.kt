package com.example.saga.order.adapter.`in`.web.response

data class GetOrderResponse(
    val orderId: String,
    val status: String,
    val totalAmount: Long,
    val items: List<OrderItemResponse>,
)
