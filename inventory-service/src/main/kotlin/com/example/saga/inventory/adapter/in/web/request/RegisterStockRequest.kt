package com.example.saga.inventory.adapter.`in`.web.request

data class RegisterStockRequest(
    val name: String,
    val quantity: Int,
    val price: Int,
)
