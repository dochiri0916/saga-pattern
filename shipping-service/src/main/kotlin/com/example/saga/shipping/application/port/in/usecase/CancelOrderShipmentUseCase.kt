package com.example.saga.shipping.application.port.`in`.usecase

import com.example.saga.shipping.application.port.`in`.command.CancelOrderShipmentCommand

interface CancelOrderShipmentUseCase {

    fun cancel(command: CancelOrderShipmentCommand)
}
