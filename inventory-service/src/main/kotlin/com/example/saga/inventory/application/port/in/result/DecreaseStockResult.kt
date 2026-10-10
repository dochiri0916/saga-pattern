package com.example.saga.inventory.application.port.`in`.result

import com.example.saga.inventory.domain.Stock

data class DecreaseStockResult(
    val stockId: String,
    val quantity: Int,
) {
    companion object {
        fun from(stock: Stock): DecreaseStockResult = DecreaseStockResult(
            stockId = stock.id.toString(),
            quantity = stock.quantity.value,
        )
    }
}
