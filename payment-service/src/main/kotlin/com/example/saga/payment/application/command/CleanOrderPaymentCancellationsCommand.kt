package com.example.saga.payment.application.command

import java.time.Duration

data class CleanOrderPaymentCancellationsCommand(

    val retention: Duration,

    val limit: Int,
)
