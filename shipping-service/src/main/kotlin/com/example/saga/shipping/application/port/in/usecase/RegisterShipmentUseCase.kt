package com.example.saga.shipping.application.port.`in`.usecase

import com.example.saga.shipping.application.port.`in`.command.RegisterShipmentCommand

interface RegisterShipmentUseCase {

    fun register(command: RegisterShipmentCommand)
}
