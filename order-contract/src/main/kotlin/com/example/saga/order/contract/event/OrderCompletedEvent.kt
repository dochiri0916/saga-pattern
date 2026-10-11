package com.example.saga.order.contract.event

data class OrderCompletedEvent(
    val orderId: String,
    val totalAmount: Long,
    val items: List<OrderCompletedItem>,
) {
    companion object { const val EVENT_TYPE = "ORDER_COMPLETED" }
}

data class OrderCompletedItem(val stockId: String, val quantity: Int)
