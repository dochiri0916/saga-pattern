package com.example.saga.inventory.domain.exception

class InvalidStockAmountException(val amount: Int) : StockDomainException()
