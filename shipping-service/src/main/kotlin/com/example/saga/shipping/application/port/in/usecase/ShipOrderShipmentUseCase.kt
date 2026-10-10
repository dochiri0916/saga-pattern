package com.example.saga.shipping.application.port.`in`.usecase

import com.example.saga.shipping.application.port.`in`.command.ShipOrderShipmentCommand

interface ShipOrderShipmentUseCase {

    fun ship(command: ShipOrderShipmentCommand)
}
