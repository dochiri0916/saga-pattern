package com.example.saga.payment.application.port.`in`.command

import java.util.UUID

data class PayCommand(
    val orderId: UUID,
    val amount: Long,
)
