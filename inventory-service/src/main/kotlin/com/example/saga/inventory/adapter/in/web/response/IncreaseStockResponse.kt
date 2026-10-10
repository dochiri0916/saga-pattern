package com.example.saga.inventory.adapter.`in`.web.response

import com.example.saga.inventory.application.port.`in`.result.IncreaseStockResult

data class IncreaseStockResponse(
    val stockId: String,
    val quantity: Int,
) {
    companion object {
        fun from(result: IncreaseStockResult): IncreaseStockResponse = IncreaseStockResponse(
            stockId = result.stockId,
            quantity = result.quantity,
        )
    }
}
