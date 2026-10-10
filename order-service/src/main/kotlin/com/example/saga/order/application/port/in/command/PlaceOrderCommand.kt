package com.example.saga.order.application.port.`in`.command

import java.util.UUID

data class PlaceOrderCommand(
    val items: List<PlaceOrderItemCommand>,

    val idempotencyKey: String? = null,
)

data class PlaceOrderItemCommand(
    val stockId: UUID,
    val quantity: Int,
)
