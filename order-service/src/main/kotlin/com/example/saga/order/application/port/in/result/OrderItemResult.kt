package com.example.saga.order.application.port.`in`.result

import com.example.saga.order.domain.OrderItem

data class OrderItemResult(
    val itemId: String,
    val stockId: String,
    val quantity: Int,
    val unitPrice: Int,
    val amount: Long,
    val status: String,
) {
    companion object {
        fun from(item: OrderItem): OrderItemResult = OrderItemResult(
            itemId = item.id.toString(),
            stockId = item.stockReferenceId.toString(),
            quantity = item.quantity.value,
            unitPrice = item.unitPrice.value,
            amount = item.amount.value,
            status = item.status.name,
        )
    }
}
