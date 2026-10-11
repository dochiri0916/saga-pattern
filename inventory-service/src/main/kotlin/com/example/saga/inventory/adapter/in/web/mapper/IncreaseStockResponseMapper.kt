package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.response.IncreaseStockResponse
import com.example.saga.inventory.application.result.IncreaseStockResult

fun IncreaseStockResult.toResponse(): IncreaseStockResponse = IncreaseStockResponse(
    stockId = this.stockId,
    quantity = this.quantity,
)
