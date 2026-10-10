package com.example.saga.inventory.domain.exception

sealed class StockDomainException(cause: Throwable? = null) : RuntimeException(cause)
