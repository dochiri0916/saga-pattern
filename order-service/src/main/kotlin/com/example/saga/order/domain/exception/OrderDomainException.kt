package com.example.saga.order.domain.exception

sealed class OrderDomainException(cause: Throwable? = null) : RuntimeException(cause)
