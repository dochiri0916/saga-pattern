package com.example.saga.order.adapter.`in`.web.mapper

import com.example.saga.order.adapter.`in`.requireV7
import com.example.saga.order.adapter.`in`.web.request.PlaceOrderRequest
import com.example.saga.order.application.command.PlaceOrderCommand
import com.example.saga.order.application.command.PlaceOrderItemCommand

object PlaceOrderRequestMapper {
    fun toCommand(request: PlaceOrderRequest): PlaceOrderCommand = PlaceOrderCommand(
        request.items.map { PlaceOrderItemCommand(it.stockId.requireV7("stockId"), it.quantity) },
    )
    fun canonicalRequest(request: PlaceOrderRequest): String = request.items
        .map { "${it.stockId}:${it.quantity}" }.sorted().joinToString(",")
}
