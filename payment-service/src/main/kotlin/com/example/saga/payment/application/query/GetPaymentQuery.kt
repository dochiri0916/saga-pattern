package com.example.saga.payment.application.query

import java.util.UUID

data class GetPaymentQuery(
    val paymentId: UUID,
)
