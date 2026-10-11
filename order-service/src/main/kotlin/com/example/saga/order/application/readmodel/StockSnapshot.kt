package com.example.saga.order.application.readmodel

import com.example.saga.order.domain.order.vo.StockReferenceId

data class StockSnapshot(
    val stockReferenceId: StockReferenceId,
    val name: String,
    val price: Int,
    val quantity: Int,
    val version: Long,
)
