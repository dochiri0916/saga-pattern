package com.example.saga.inventory.domain.exception

class NegativeStockQuantityException(val quantity: Int) : StockDomainException()
