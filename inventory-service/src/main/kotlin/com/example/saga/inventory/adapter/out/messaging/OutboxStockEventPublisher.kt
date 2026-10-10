package com.example.saga.inventory.adapter.out.messaging

import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRevision
import com.example.saga.inventory.domain.Stock
import com.example.saga.platform.outbox.OutboxAppender
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class OutboxStockEventPublisher(
    private val outboxAppender: OutboxAppender,
    private val stockTopicProperties: StockTopicProperties,
    private val jsonMapper: JsonMapper,
) : StockEventPublisher {

    override fun publishStockChanged(stock: Stock, revision: StockRevision) = outboxAppender.append(
        StockChangedMessage.EVENT_TYPE,
        stockTopicProperties.name,
        stock.id.toString(),
        jsonMapper.writeValueAsString(StockChangedMessage.from(stock, revision)),
    )
}
