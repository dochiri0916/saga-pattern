package com.example.saga.payment.adapter.`in`.web.controller

import com.example.saga.payment.adapter.`in`.requireV7
import com.example.saga.payment.application.command.CancelOrderPaymentCommand
import com.example.saga.payment.application.port.`in`.CancelOrderPaymentUseCase
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order-payments/{orderId}")
class OrderPaymentController(
    private val cancelOrderPaymentUseCase: CancelOrderPaymentUseCase,
) {

    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable orderId: UUID) = cancelOrderPaymentUseCase.cancel(CancelOrderPaymentCommand(orderId.requireV7("orderId")))
}
