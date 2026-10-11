package com.example.saga.order.adapter.`in`.web.mapper

import com.example.saga.order.adapter.`in`.web.response.CancelOrderResponse
import com.example.saga.order.application.result.CancelOrderResult

fun CancelOrderResult.toResponse(): CancelOrderResponse = CancelOrderResponse(
    orderId = this.orderId,
    status = this.status,
)
