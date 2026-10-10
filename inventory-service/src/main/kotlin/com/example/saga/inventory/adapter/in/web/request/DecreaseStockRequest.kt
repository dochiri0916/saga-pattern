package com.example.saga.inventory.adapter.`in`.web.request

import com.example.saga.inventory.application.port.`in`.command.DecreaseStockCommand
import java.util.UUID

data class DecreaseStockRequest(
    val quantity: Int,
) {
    fun toCommand(stockId: UUID): DecreaseStockCommand = DecreaseStockCommand(
        stockId = stockId,
        quantity = quantity,
    )
}
