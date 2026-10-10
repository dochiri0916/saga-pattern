package com.example.saga.inventory.adapter.`in`.web.request

import com.example.saga.inventory.adapter.`in`.requireV7
import com.example.saga.inventory.application.port.`in`.command.DecreaseOrderStockCommand
import com.example.saga.inventory.application.port.`in`.command.OrderStockItemCommand
import java.util.UUID

data class DecreaseOrderStockRequest(
    val items: List<OrderStockItemRequest>,
) {
    fun toCommand(orderId: UUID): DecreaseOrderStockCommand = DecreaseOrderStockCommand(
        orderId = orderId,
        items = items.map { it.toCommand() },
    )
}

data class OrderStockItemRequest(
    val stockId: UUID,
    val quantity: Int,
) {
    fun toCommand(): OrderStockItemCommand = OrderStockItemCommand(
        stockId = stockId.requireV7("stockId"),
        quantity = quantity,
    )
}
