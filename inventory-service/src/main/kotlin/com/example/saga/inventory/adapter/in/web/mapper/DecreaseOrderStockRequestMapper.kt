package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.requireV7
import com.example.saga.inventory.adapter.`in`.web.request.DecreaseOrderStockRequest
import com.example.saga.inventory.adapter.`in`.web.request.OrderStockItemRequest
import com.example.saga.inventory.application.command.DecreaseOrderStockCommand
import com.example.saga.inventory.application.command.OrderStockItemCommand
import java.util.UUID

fun DecreaseOrderStockRequest.toCommand(orderId: UUID): DecreaseOrderStockCommand = DecreaseOrderStockCommand(
    orderId = orderId,
    items = items.map { it.toCommand() },
)

fun OrderStockItemRequest.toCommand(): OrderStockItemCommand = OrderStockItemCommand(
    stockId = stockId.requireV7("stockId"),
    quantity = quantity,
)
