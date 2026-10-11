package com.example.saga.inventory.application.command

import java.util.UUID

data class DecreaseOrderStockCommand(
    val orderId: UUID,
    val items: List<OrderStockItemCommand>,
)

data class RestoreOrderStockCommand(
    val orderId: UUID,
    val items: List<OrderStockItemCommand>,
)

data class OrderStockItemCommand(
    val stockId: UUID,
    val quantity: Int,
)
