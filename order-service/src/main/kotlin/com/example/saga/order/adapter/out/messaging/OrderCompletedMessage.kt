package com.example.saga.order.adapter.out.messaging

import com.example.saga.order.domain.Order

data class OrderCompletedMessage(
    val orderId: String,
    val totalAmount: Long,
    val items: List<OrderCompletedItemMessage>,
) {
    companion object {
        const val EVENT_TYPE = "ORDER_COMPLETED"

        fun from(order: Order): OrderCompletedMessage = OrderCompletedMessage(
            orderId = order.id.toString(),
            totalAmount = order.totalAmount.value,
            items = order.items.map { OrderCompletedItemMessage(it.stockReferenceId.toString(), it.quantity.value) },
        )
    }
}

data class OrderCompletedItemMessage(
    val stockId: String,
    val quantity: Int,
)
