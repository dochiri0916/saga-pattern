package com.example.saga.inventory.contract.event

data class StockChangedEvent(
    val stockId: String,
    val name: String,
    val price: Int,
    val quantity: Int,
    val version: Long,
) {
    companion object { const val EVENT_TYPE = "STOCK_CHANGED" }
}
