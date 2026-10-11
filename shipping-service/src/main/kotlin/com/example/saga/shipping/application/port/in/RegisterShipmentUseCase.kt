package com.example.saga.shipping.application.port.`in`

import com.example.saga.shipping.application.command.RegisterShipmentCommand

interface RegisterShipmentUseCase {

    fun register(command: RegisterShipmentCommand)
}
