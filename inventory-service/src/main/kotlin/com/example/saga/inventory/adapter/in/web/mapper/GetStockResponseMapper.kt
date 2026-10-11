package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.response.GetStockResponse
import com.example.saga.inventory.application.result.GetStockResult

fun GetStockResult.toResponse(): GetStockResponse = GetStockResponse(
    stockId = this.stockId,
    name = this.name,
    quantity = this.quantity,
    price = this.price,
)
