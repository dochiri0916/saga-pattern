package com.example.saga.shipping.application.command

import java.util.UUID

data class RegisterShipmentCommand(

    val orderId: UUID,
)
