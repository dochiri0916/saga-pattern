package com.example.saga.order.domain.exception

class OrderItemsLimitExceededException(val size: Int, val maxSize: Int) : OrderDomainException()
