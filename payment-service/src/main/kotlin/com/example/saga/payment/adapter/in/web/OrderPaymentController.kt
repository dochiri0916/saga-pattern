package com.example.saga.payment.adapter.`in`.web

import com.example.saga.payment.adapter.`in`.requireV7
import com.example.saga.payment.application.port.`in`.command.CancelOrderPaymentCommand
import com.example.saga.payment.application.port.`in`.usecase.CancelOrderPaymentUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/order-payments/{orderId}")
class OrderPaymentController(
    private val cancelOrderPaymentUseCase: CancelOrderPaymentUseCase,
) {

    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable orderId: UUID) = cancelOrderPaymentUseCase.cancel(CancelOrderPaymentCommand(orderId.requireV7("orderId")))
}
