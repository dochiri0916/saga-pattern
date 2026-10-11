package com.example.saga.inventory.application.integration.mapper

import com.example.saga.inventory.application.port.out.EventPublicationPort
import com.example.saga.inventory.domain.stock.Stock
import com.example.saga.inventory.domain.stock.event.StockChanged
import com.example.saga.inventory.domain.stock.vo.StockRevision

fun EventPublicationPort.publishStockChanged(stock: Stock, revision: StockRevision) =
    publish(StockIntegrationEventMapper.map(StockChanged(stock, revision)))
