package com.example.saga.shipping.application.port.`in`

import com.example.saga.shipping.application.command.ShipOrderShipmentCommand

interface ShipOrderShipmentUseCase {

    fun ship(command: ShipOrderShipmentCommand)
}
