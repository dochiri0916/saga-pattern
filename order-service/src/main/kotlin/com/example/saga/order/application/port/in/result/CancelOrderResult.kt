package com.example.saga.order.application.port.`in`.result

import com.example.saga.order.domain.Order

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
