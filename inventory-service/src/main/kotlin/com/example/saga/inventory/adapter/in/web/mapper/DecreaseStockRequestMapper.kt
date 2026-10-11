package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.request.DecreaseStockRequest
import com.example.saga.inventory.application.command.DecreaseStockCommand
import java.util.UUID

fun DecreaseStockRequest.toCommand(stockId: UUID): DecreaseStockCommand = DecreaseStockCommand(
    stockId = stockId,
    quantity = quantity,
)
