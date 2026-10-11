package com.example.saga.payment.adapter.`in`.web.controller

import com.example.saga.payment.adapter.`in`.requireV7
import com.example.saga.payment.adapter.`in`.web.mapper.toCommand
import com.example.saga.payment.adapter.`in`.web.mapper.toResponse
import com.example.saga.payment.adapter.`in`.web.request.PayRequest
import com.example.saga.payment.adapter.`in`.web.response.GetPaymentResponse
import com.example.saga.payment.adapter.`in`.web.response.PayResponse
import com.example.saga.payment.application.port.`in`.GetPaymentUseCase
import com.example.saga.payment.application.port.`in`.PayUseCase
import com.example.saga.payment.application.query.GetPaymentQuery
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val payUseCase: PayUseCase,
    private val getPaymentUseCase: GetPaymentUseCase,
) {

    @PostMapping
    fun pay(@RequestBody request: PayRequest): ResponseEntity<PayResponse> {
        val response = payUseCase.pay(request.toCommand()).toResponse()
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{paymentId}")
            .buildAndExpand(response.paymentId)
            .toUri()
        return ResponseEntity.created(location).body(response)
    }

    @GetMapping("/{paymentId}")
    fun get(@PathVariable paymentId: UUID): GetPaymentResponse =
        getPaymentUseCase.get(GetPaymentQuery(paymentId.requireV7("paymentId"))).toResponse()
}
