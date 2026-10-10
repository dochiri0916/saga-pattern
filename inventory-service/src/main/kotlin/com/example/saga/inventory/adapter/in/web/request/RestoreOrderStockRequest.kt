package com.example.saga.inventory.adapter.`in`.web.request

import com.example.saga.inventory.application.port.`in`.command.RestoreOrderStockCommand
import java.util.UUID

data class RestoreOrderStockRequest(
    val items: List<OrderStockItemRequest>,
) {
    fun toCommand(orderId: UUID): RestoreOrderStockCommand = RestoreOrderStockCommand(
        orderId = orderId,
        items = items.map { it.toCommand() },
    )
}
