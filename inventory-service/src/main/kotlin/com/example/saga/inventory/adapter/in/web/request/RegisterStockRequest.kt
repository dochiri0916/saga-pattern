package com.example.saga.inventory.adapter.`in`.web.request

import com.example.saga.inventory.application.port.`in`.command.RegisterStockCommand

data class RegisterStockRequest(
    val name: String,
    val quantity: Int,
    val price: Int,
) {
    fun toCommand(): RegisterStockCommand = RegisterStockCommand(
        name = name,
        quantity = quantity,
        price = price,
    )
}
