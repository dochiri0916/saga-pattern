package com.example.saga.order.adapter.`in`.web.response

data class PlaceOrderResponse(
    val orderId: String,
    val status: String,
    val totalAmount: Long,
    val items: List<OrderItemResponse>,
)
