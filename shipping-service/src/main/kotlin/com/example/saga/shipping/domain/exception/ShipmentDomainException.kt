package com.example.saga.shipping.domain.exception

sealed class ShipmentDomainException(cause: Throwable? = null) : RuntimeException(cause)
