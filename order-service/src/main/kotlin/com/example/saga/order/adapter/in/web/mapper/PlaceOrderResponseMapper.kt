package com.example.saga.order.adapter.`in`.web.mapper

import com.example.saga.order.adapter.`in`.web.response.PlaceOrderResponse
import com.example.saga.order.application.result.PlaceOrderResult

fun PlaceOrderResult.toResponse(): PlaceOrderResponse = PlaceOrderResponse(
    orderId = this.orderId,
    status = this.status,
    totalAmount = this.totalAmount,
    items = this.items.map { it.toResponse() },
)
