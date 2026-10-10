package com.example.saga.inventory.application.port.`in`.query

import java.util.UUID

data class GetStockQuery(
    val stockId: UUID,
)
