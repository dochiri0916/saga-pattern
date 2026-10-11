package com.example.saga.inventory.domain.stock.exception

sealed class StockDomainException(cause: Throwable? = null) : RuntimeException(cause)

class InsufficientStockException(val currentStock: Int, val requestedQuantity: Int) : StockDomainException()

class InvalidStockAmountException(val amount: Int) : StockDomainException()

class NegativeStockPriceException(val price: Int) : StockDomainException()

class NegativeStockQuantityException(val quantity: Int) : StockDomainException()

class StockNameRequiredException : StockDomainException()

class StockOverflowException(val currentStock: Int, val requestedQuantity: Int, cause: ArithmeticException) :
    StockDomainException(cause)
