package com.example.saga.inventory.adapter.`in`.web.response

import com.example.saga.inventory.application.port.`in`.result.GetStockResult

data class GetStockResponse(
    val stockId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
) {
    companion object {
        fun from(result: GetStockResult): GetStockResponse = GetStockResponse(
            stockId = result.stockId,
            name = result.name,
            quantity = result.quantity,
            price = result.price,
        )
    }
}
