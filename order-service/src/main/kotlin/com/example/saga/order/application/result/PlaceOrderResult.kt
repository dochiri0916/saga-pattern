package com.example.saga.order.application.result

import com.example.saga.order.domain.order.Order

data class PlaceOrderResult(
    val orderId: String,
    val status: String,
    val totalAmount: Long,
    val items: List<OrderItemResult>,
) {
    companion object {
        fun from(order: Order): PlaceOrderResult = PlaceOrderResult(
            orderId = order.id.toString(),
            status = order.status.name,
            totalAmount = order.totalAmount.value,
            items = order.items.map { OrderItemResult.from(it) },
        )
    }
}
