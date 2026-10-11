package com.example.saga.inventory.application.result

import com.example.saga.inventory.domain.stock.Stock

data class IncreaseStockResult(
    val stockId: String,
    val quantity: Int,
) {
    companion object {
        fun from(stock: Stock): IncreaseStockResult = IncreaseStockResult(
            stockId = stock.id.toString(),
            quantity = stock.quantity.value,
        )
    }
}
