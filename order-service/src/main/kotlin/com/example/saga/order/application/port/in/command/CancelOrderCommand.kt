package com.example.saga.order.application.port.`in`.command

import java.util.UUID

data class CancelOrderCommand(
    val orderId: UUID,
)
