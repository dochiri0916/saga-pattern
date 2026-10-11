package com.example.saga.shipping.application.command

import java.util.UUID

data class ShipOrderShipmentCommand(
    val orderId: UUID,
)
