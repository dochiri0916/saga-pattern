package com.example.saga.inventory.application.exception

sealed class StockApplicationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
