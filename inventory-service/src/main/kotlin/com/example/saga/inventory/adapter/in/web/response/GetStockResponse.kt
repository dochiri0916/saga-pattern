package com.example.saga.inventory.adapter.`in`.web.response

data class GetStockResponse(
    val stockId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
)
