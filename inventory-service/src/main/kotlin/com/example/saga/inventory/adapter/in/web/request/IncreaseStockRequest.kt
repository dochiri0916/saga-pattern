package com.example.saga.inventory.adapter.`in`.web.request

import com.example.saga.inventory.application.port.`in`.command.IncreaseStockCommand
import java.util.UUID

data class IncreaseStockRequest(
    val quantity: Int,
) {
    fun toCommand(stockId: UUID): IncreaseStockCommand = IncreaseStockCommand(
        stockId = stockId,
        quantity = quantity,
    )
}
