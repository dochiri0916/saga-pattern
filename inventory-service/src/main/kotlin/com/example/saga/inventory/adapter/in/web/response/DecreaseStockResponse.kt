package com.example.saga.inventory.adapter.`in`.web.response

import com.example.saga.inventory.application.port.`in`.result.DecreaseStockResult

data class DecreaseStockResponse(
    val stockId: String,
    val quantity: Int,
) {
    companion object {
        fun from(result: DecreaseStockResult): DecreaseStockResponse = DecreaseStockResponse(
            stockId = result.stockId,
            quantity = result.quantity,
        )
    }
}
