package com.example.saga.order.application.exception

sealed class OrderApplicationException(cause: Throwable? = null) : RuntimeException(cause)

class OrderNotFoundException(val orderId: String) : OrderApplicationException()

class OrderSagaNotFoundException(val orderId: String) : OrderApplicationException()

class StockNotFoundException(val stockId: String) : OrderApplicationException()

class OrderRejectedException(
    val orderId: String,

    val reason: String?,
) : OrderApplicationException()

class OrderCancelRejectedException(
    val orderId: String,

    val reason: String?,
) : OrderApplicationException()

class OrderCancelPeriodExpiredException(val orderId: String) : OrderApplicationException()

class OrderConcurrentUpdateException(val orderId: String, cause: Throwable? = null) :
    OrderApplicationException(cause)
