package com.example.saga.payment.adapter.`in`.web.mapper

import com.example.saga.payment.adapter.`in`.requireV7
import com.example.saga.payment.adapter.`in`.web.request.PayRequest
import com.example.saga.payment.application.command.PayCommand

fun PayRequest.toCommand(): PayCommand = PayCommand(
    orderId = orderId.requireV7("orderId"),
    amount = amount,
)
