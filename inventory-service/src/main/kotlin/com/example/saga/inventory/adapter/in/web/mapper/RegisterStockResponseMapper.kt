package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.response.RegisterStockResponse
import com.example.saga.inventory.application.result.RegisterStockResult

fun RegisterStockResult.toResponse(): RegisterStockResponse = RegisterStockResponse(
    stockId = this.stockId,
    name = this.name,
    quantity = this.quantity,
    price = this.price,
)
