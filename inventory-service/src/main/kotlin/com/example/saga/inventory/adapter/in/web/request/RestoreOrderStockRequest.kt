package com.example.saga.inventory.adapter.`in`.web.request

data class RestoreOrderStockRequest(
    val items: List<OrderStockItemRequest>,
)
