package com.example.saga.order.application.port.out

open class ParticipantCallException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ParticipantUnavailableException(message: String, cause: Throwable? = null) : ParticipantCallException(message, cause)
