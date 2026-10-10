package com.example.saga.payment.application.port.`in`.query

import java.util.UUID

data class GetPaymentQuery(
    val paymentId: UUID,
)
