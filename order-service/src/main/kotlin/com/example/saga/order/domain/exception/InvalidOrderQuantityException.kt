package com.example.saga.order.domain.exception

class InvalidOrderQuantityException(val quantity: Int) : OrderDomainException()
