package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.request.RestoreOrderStockRequest
import com.example.saga.inventory.application.command.RestoreOrderStockCommand
import java.util.UUID

fun RestoreOrderStockRequest.toCommand(orderId: UUID): RestoreOrderStockCommand = RestoreOrderStockCommand(
    orderId = orderId,
    items = items.map { it.toCommand() },
)
