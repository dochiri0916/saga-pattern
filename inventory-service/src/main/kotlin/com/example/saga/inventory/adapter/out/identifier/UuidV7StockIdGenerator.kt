package com.example.saga.inventory.adapter.out.identifier

import com.example.saga.inventory.application.port.out.StockIdGenerator
import com.example.saga.inventory.domain.stock.vo.StockId
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Component

@Component
class UuidV7StockIdGenerator : StockIdGenerator {
    override fun generate(): StockId = StockId.of(UuidCreator.getTimeOrderedEpoch())
}
