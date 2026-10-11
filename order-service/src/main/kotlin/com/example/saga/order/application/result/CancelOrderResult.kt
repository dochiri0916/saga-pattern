package com.example.saga.order.application.result

import com.example.saga.order.domain.order.Order

data class CancelOrderResult(
    val orderId: String,
    val status: String,
) {
    companion object {
        fun from(order: Order): CancelOrderResult = CancelOrderResult(
            orderId = order.id.toString(),
            status = order.status.name,
        )
    }
}
