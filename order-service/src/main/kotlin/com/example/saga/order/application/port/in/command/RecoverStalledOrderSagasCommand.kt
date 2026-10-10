package com.example.saga.order.application.port.`in`.command

import java.time.Duration

data class RecoverStalledOrderSagasCommand(

    val stalledFor: Duration,

    val limit: Int,
)
