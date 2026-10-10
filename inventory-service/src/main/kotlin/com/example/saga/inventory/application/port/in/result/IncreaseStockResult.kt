package com.example.saga.inventory.application.port.`in`.result

import com.example.saga.inventory.domain.Stock

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
