package com.example.saga.inventory.application.port.`in`.command

import java.util.UUID

data class DecreaseStockCommand(
    val stockId: UUID,
    val quantity: Int,
)
