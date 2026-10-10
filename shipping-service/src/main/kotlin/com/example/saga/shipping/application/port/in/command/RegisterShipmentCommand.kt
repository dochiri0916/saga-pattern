package com.example.saga.shipping.application.port.`in`.command

import java.util.UUID

data class RegisterShipmentCommand(

    val eventId: UUID,
    val orderId: UUID,
)
