package com.example.saga.inventory.contract.command

data class DecreaseOrderStock(val items: List<OrderStockItem>)
data class RestoreOrderStock(val items: List<OrderStockItem>)
data class OrderStockItem(val stockId: String, val quantity: Int)
