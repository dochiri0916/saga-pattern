package com.example.saga.inventory.application.integration.mapper

import com.example.saga.inventory.contract.event.StockChangedEvent
import com.example.saga.inventory.domain.stock.event.StockChanged

object StockIntegrationEventMapper {
    fun map(event: StockChanged): StockChangedEvent = with(event.stock) {
        StockChangedEvent(id.toString(), name.value, price.value, quantity.value, event.revision.value)
    }
}
