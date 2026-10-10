package com.example.saga.order.adapter.`in`.web.response

import com.example.saga.order.application.port.`in`.result.PlaceOrderResult

data class PlaceOrderResponse(
    val orderId: String,
    val status: String,
    val totalAmount: Long,
    val items: List<OrderItemResponse>,
) {
    companion object {
        fun from(result: PlaceOrderResult): PlaceOrderResponse = PlaceOrderResponse(
            orderId = result.orderId,
            status = result.status,
            totalAmount = result.totalAmount,
            items = result.items.map { OrderItemResponse.from(it) },
        )
    }
}
