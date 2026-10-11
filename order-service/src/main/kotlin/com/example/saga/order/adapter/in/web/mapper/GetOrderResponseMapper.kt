package com.example.saga.order.adapter.`in`.web.mapper

import com.example.saga.order.adapter.`in`.web.response.GetOrderResponse
import com.example.saga.order.application.result.GetOrderResult

fun GetOrderResult.toResponse(): GetOrderResponse = GetOrderResponse(
    orderId = this.orderId,
    status = this.status,
    totalAmount = this.totalAmount,
    items = this.items.map { it.toResponse() },
)
