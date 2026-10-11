package com.example.saga.order.adapter.`in`.web.mapper

import com.example.saga.order.adapter.`in`.web.response.OrderItemResponse
import com.example.saga.order.application.result.OrderItemResult

fun OrderItemResult.toResponse(): OrderItemResponse = OrderItemResponse(
    itemId = this.itemId,
    stockId = this.stockId,
    quantity = this.quantity,
    unitPrice = this.unitPrice,
    amount = this.amount,
    status = this.status,
)
