package com.example.saga.payment.adapter.`in`.web.request

import java.util.UUID

data class PayRequest(
    val orderId: UUID,
    val amount: Long,
)
