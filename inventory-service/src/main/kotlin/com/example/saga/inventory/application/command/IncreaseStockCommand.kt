package com.example.saga.inventory.application.command

import java.util.UUID

data class IncreaseStockCommand(
    val stockId: UUID,
    val quantity: Int,
)
