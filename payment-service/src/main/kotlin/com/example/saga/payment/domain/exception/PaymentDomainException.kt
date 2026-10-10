package com.example.saga.payment.domain.exception

sealed class PaymentDomainException(cause: Throwable? = null) : RuntimeException(cause)
