package com.example.saga.payment.adapter.`in`.web.mapper

import com.example.saga.payment.adapter.`in`.web.response.PayResponse
import com.example.saga.payment.application.result.PayResult

fun PayResult.toResponse(): PayResponse = PayResponse(
    paymentId = this.paymentId,
    orderId = this.orderId,
    amount = this.amount,
    status = this.status,
)
