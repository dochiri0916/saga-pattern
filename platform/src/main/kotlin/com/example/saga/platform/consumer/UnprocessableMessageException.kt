package com.example.saga.platform.consumer

class UnprocessableMessageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
