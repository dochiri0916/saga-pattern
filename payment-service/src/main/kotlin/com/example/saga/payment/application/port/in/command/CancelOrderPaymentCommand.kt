package com.example.saga.payment.application.port.`in`.command

import java.util.UUID

data class CancelOrderPaymentCommand(
    val orderId: UUID,
)
