package com.example.saga.payment.application.command

import java.util.UUID

data class PayCommand(
    val orderId: UUID,
    val amount: Long,
)
