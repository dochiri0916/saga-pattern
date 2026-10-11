package com.example.saga.inventory.adapter.`in`.web.response

data class RegisterStockResponse(
    val stockId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
)
