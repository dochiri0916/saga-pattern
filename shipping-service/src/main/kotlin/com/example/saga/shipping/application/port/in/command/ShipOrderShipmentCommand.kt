package com.example.saga.shipping.application.port.`in`.command

import java.util.UUID

data class ShipOrderShipmentCommand(
    val orderId: UUID,
)
