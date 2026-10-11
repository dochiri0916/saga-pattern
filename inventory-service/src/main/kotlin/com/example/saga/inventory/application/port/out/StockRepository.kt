package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.domain.stock.Stock
import com.example.saga.inventory.domain.stock.vo.StockId
import com.example.saga.inventory.domain.stock.vo.StockRevision

interface StockRepository {

    fun save(stock: Stock): StockRevision

    fun findById(id: StockId): Stock?

    fun findByIdForUpdate(id: StockId): Stock?
}
