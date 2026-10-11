package com.example.saga.inventory.application.result

import com.example.saga.inventory.domain.stock.Stock

data class RegisterStockResult(
    val stockId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
) {
    companion object {
        fun from(stock: Stock): RegisterStockResult = RegisterStockResult(
            stockId = stock.id.toString(),
            name = stock.name.value,
            quantity = stock.quantity.value,
            price = stock.price.value,
        )
    }
}
