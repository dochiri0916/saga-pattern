package com.example.saga.inventory.domain.exception

class InsufficientStockException(val currentStock: Int, val requestedQuantity: Int) : StockDomainException()
