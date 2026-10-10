package com.example.saga.order.application.port.`in`.command

import java.time.Duration

data class CleanOrderIdempotencyKeysCommand(

    val retention: Duration,

    val limit: Int,
)
