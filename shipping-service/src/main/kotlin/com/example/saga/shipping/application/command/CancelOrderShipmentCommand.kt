package com.example.saga.shipping.application.command

import java.util.UUID

data class CancelOrderShipmentCommand(
    val orderId: UUID,
)
