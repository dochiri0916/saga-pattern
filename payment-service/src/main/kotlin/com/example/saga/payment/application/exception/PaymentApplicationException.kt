package com.example.saga.payment.application.exception

sealed class PaymentApplicationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
