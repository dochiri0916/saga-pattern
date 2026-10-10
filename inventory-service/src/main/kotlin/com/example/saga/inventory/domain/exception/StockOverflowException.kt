package com.example.saga.inventory.domain.exception

class StockOverflowException(val currentStock: Int, val requestedQuantity: Int, cause: ArithmeticException) :
    StockDomainException(cause)
