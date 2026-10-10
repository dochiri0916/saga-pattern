package com.example.saga.order.application.port.`in`.query

import java.util.UUID

data class GetOrderQuery(
    val orderId: UUID,
)
