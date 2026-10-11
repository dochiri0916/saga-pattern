package com.example.saga.inventory.application.result

import com.example.saga.inventory.domain.stock.Stock

data class GetStockResult(
    val stockId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
) {
    companion object {
        fun from(stock: Stock): GetStockResult = GetStockResult(
            stockId = stock.id.toString(),
            name = stock.name.value,
            quantity = stock.quantity.value,
            price = stock.price.value,
        )
    }
}
