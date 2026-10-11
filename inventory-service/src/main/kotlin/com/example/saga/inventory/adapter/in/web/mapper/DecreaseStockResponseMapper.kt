package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.response.DecreaseStockResponse
import com.example.saga.inventory.application.result.DecreaseStockResult

fun DecreaseStockResult.toResponse(): DecreaseStockResponse = DecreaseStockResponse(
    stockId = this.stockId,
    quantity = this.quantity,
)
