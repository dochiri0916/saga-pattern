package com.example.saga.order.domain.exception

class OrderAmountOverflowException(cause: ArithmeticException) : OrderDomainException(cause)
