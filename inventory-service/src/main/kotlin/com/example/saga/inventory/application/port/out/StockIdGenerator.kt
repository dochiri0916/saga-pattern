package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.domain.stock.vo.StockId

interface StockIdGenerator {

    fun generate(): StockId

}
