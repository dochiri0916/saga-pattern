package com.example.saga.shipping.application.port.`in`

import com.example.saga.shipping.application.command.CancelOrderShipmentCommand

interface CancelOrderShipmentUseCase {

    fun cancel(command: CancelOrderShipmentCommand)
}
