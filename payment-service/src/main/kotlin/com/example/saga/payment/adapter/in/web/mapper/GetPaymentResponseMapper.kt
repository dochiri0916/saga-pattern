package com.example.saga.payment.adapter.`in`.web.mapper

import com.example.saga.payment.adapter.`in`.web.response.GetPaymentResponse
import com.example.saga.payment.application.result.GetPaymentResult

fun GetPaymentResult.toResponse(): GetPaymentResponse = GetPaymentResponse(
    paymentId = this.paymentId,
    orderId = this.orderId,
    amount = this.amount,
    status = this.status,
)
