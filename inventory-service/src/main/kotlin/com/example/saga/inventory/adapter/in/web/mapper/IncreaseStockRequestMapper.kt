package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.request.IncreaseStockRequest
import com.example.saga.inventory.application.command.IncreaseStockCommand
import java.util.UUID

fun IncreaseStockRequest.toCommand(stockId: UUID): IncreaseStockCommand = IncreaseStockCommand(
    stockId = stockId,
    quantity = quantity,
)
