package com.example.saga.shipping.adapter.`in`.web.controller

import com.example.saga.shipping.adapter.`in`.requireV7
import com.example.saga.shipping.application.command.CancelOrderShipmentCommand
import com.example.saga.shipping.application.command.ShipOrderShipmentCommand
import com.example.saga.shipping.application.port.`in`.CancelOrderShipmentUseCase
import com.example.saga.shipping.application.port.`in`.ShipOrderShipmentUseCase
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order-shipments/{orderId}")
class OrderShipmentController(
    private val cancelOrderShipmentUseCase: CancelOrderShipmentUseCase,
    private val shipOrderShipmentUseCase: ShipOrderShipmentUseCase,
) {

    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable orderId: UUID) = cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId.requireV7("orderId")))

    @PostMapping("/ship")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun ship(@PathVariable orderId: UUID) = shipOrderShipmentUseCase.ship(ShipOrderShipmentCommand(orderId.requireV7("orderId")))
}
