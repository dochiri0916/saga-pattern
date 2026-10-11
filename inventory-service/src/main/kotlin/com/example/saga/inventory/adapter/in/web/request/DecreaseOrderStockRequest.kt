package com.example.saga.inventory.adapter.`in`.web.request

import java.util.UUID

data class DecreaseOrderStockRequest(
    val items: List<OrderStockItemRequest>,
)

data class OrderStockItemRequest(
    val stockId: UUID,
    val quantity: Int,
)
