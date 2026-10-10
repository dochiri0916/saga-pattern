package com.example.saga.order.application.exception

sealed class OrderApplicationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
