package com.example.saga.shipping.application.exception

sealed class ShipmentApplicationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
