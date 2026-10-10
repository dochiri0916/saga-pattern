package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.domain.Stock

interface StockEventPublisher {

    fun publishStockChanged(stock: Stock, revision: StockRevision)
}
