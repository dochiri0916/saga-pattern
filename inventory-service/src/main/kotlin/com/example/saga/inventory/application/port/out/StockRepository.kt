package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.domain.Stock
import com.example.saga.inventory.domain.vo.StockId

interface StockRepository {

    fun save(stock: Stock): StockRevision

    fun findById(id: StockId): Stock?

    fun findByIdForUpdate(id: StockId): Stock?
}
