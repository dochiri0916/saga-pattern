package com.example.saga.inventory.adapter.`in`.web.response

import com.example.saga.inventory.application.port.`in`.result.RegisterStockResult

data class RegisterStockResponse(
    val stockId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
) {
    companion object {
        fun from(result: RegisterStockResult): RegisterStockResponse = RegisterStockResponse(
            stockId = result.stockId,
            name = result.name,
            quantity = result.quantity,
            price = result.price,
        )
    }
}
