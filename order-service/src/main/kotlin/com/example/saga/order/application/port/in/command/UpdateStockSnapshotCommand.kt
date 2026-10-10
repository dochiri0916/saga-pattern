package com.example.saga.order.application.port.`in`.command

import java.util.UUID

data class UpdateStockSnapshotCommand(
    val stockId: UUID,
    val name: String,
    val price: Int,
    val quantity: Int,
    val version: Long,
)
