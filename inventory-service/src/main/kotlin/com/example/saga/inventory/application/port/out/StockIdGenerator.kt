package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.domain.vo.StockId

interface StockIdGenerator {

    fun generate(): StockId

}
