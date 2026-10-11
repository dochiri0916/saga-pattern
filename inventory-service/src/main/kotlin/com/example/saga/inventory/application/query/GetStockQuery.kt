package com.example.saga.inventory.application.query

import java.util.UUID

data class GetStockQuery(
    val stockId: UUID,
)
