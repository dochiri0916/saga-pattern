package com.example.saga.order.adapter.`in`.web.response

import com.example.saga.order.application.port.`in`.result.GetOrderResult

data class GetOrderResponse(
    val orderId: String,
    val status: String,
    val totalAmount: Long,
    val items: List<OrderItemResponse>,
) {
    companion object {
        fun from(result: GetOrderResult): GetOrderResponse = GetOrderResponse(
            orderId = result.orderId,
            status = result.status,
            totalAmount = result.totalAmount,
            items = result.items.map { OrderItemResponse.from(it) },
        )
    }
}
