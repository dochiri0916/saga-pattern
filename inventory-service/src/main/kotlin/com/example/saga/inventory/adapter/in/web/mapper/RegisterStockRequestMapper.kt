package com.example.saga.inventory.adapter.`in`.web.mapper

import com.example.saga.inventory.adapter.`in`.web.request.RegisterStockRequest
import com.example.saga.inventory.application.command.RegisterStockCommand

fun RegisterStockRequest.toCommand(): RegisterStockCommand = RegisterStockCommand(
    name = name,
    quantity = quantity,
    price = price,
)
