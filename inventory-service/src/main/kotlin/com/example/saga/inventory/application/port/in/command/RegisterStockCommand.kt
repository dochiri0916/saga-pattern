package com.example.saga.inventory.application.port.`in`.command

data class RegisterStockCommand(
    val name: String,
    val quantity: Int,
    val price: Int,
)
