package com.example.saga.order.adapter.`in`.web.response

import com.example.saga.order.application.port.`in`.result.OrderItemResult

data class OrderItemResponse(
    val itemId: String,
    val stockId: String,
    val quantity: Int,
    val unitPrice: Int,
    val amount: Long,
    val status: String,
) {
    companion object {
        fun from(result: OrderItemResult): OrderItemResponse = OrderItemResponse(
            itemId = result.itemId,
            stockId = result.stockId,
            quantity = result.quantity,
            unitPrice = result.unitPrice,
            amount = result.amount,
            status = result.status,
        )
    }
}
