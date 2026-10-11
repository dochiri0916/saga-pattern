package com.example.saga.payment.application.command

import java.util.UUID

data class CancelOrderPaymentCommand(
    val orderId: UUID,
)
