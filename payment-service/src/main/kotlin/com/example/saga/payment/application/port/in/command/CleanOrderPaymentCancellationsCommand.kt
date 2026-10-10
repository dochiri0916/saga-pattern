package com.example.saga.payment.application.port.`in`.command

import java.time.Duration

data class CleanOrderPaymentCancellationsCommand(

    val retention: Duration,

    val limit: Int,
)
