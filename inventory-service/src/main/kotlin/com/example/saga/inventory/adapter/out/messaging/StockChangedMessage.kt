package com.example.saga.inventory.adapter.out.messaging

import com.example.saga.inventory.application.port.out.StockRevision
import com.example.saga.inventory.domain.Stock

data class StockChangedMessage(
    val stockId: String,
    val name: String,
    val price: Int,
    val quantity: Int,
    val version: Long,
) {
    companion object {
        const val EVENT_TYPE = "STOCK_CHANGED"

        fun from(stock: Stock, revision: StockRevision): StockChangedMessage = StockChangedMessage(
            stockId = stock.id.toString(),
            name = stock.name.value,
            price = stock.price.value,
            quantity = stock.quantity.value,
            version = revision.value,
        )
    }
}
