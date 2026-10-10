package com.example.saga.order.adapter.`in`.web.response

import com.example.saga.order.application.port.`in`.result.CancelOrderResult

data class CancelOrderResponse(
    val orderId: String,
    val status: String,
) {
    companion object {
        fun from(result: CancelOrderResult): CancelOrderResponse = CancelOrderResponse(
            orderId = result.orderId,
            status = result.status,
        )
    }
}
