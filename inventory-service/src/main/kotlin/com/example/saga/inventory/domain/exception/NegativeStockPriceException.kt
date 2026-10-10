package com.example.saga.inventory.domain.exception

class NegativeStockPriceException(val price: Int) : StockDomainException()
