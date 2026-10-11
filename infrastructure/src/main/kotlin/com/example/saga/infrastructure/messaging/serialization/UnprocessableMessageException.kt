package com.example.saga.infrastructure.messaging.serialization

class UnprocessableMessageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
