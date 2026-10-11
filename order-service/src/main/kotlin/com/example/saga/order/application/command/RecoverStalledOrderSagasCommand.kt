package com.example.saga.order.application.command

import java.time.Duration

data class RecoverStalledOrderSagasCommand(

    val stalledFor: Duration,

    val limit: Int,
)
