package com.example.saga.inventory.adapter.out.messaging

import com.example.saga.infrastructure.messaging.outbox.OutboxWriter
import com.example.saga.infrastructure.messaging.serialization.EventSerializer
import com.example.saga.inventory.application.port.out.EventPublicationPort
import com.example.saga.inventory.contract.event.StockChangedEvent
import org.springframework.stereotype.Component

@Component
class EventPublicationAdapter(
    private val outboxWriter: OutboxWriter,
    private val stockTopicProperties: StockTopicProperties,
    private val eventSerializer: EventSerializer,
) : EventPublicationPort {
    override fun publish(event: StockChangedEvent) = outboxWriter.write(
        StockChangedEvent.EVENT_TYPE, stockTopicProperties.name, event.stockId, eventSerializer.serialize(event),
    )
}
