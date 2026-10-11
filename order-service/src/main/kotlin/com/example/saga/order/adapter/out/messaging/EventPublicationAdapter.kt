package com.example.saga.order.adapter.out.messaging

import com.example.saga.infrastructure.messaging.outbox.OutboxWriter
import com.example.saga.infrastructure.messaging.serialization.EventSerializer
import com.example.saga.order.application.port.out.EventPublicationPort
import com.example.saga.order.contract.event.OrderCompletedEvent
import org.springframework.stereotype.Component

@Component
class EventPublicationAdapter(
    private val outboxWriter: OutboxWriter,
    private val orderCompletedTopicProperties: OrderCompletedTopicProperties,
    private val eventSerializer: EventSerializer,
) : EventPublicationPort {
    override fun publish(event: OrderCompletedEvent) = outboxWriter.write(
        OrderCompletedEvent.EVENT_TYPE, orderCompletedTopicProperties.name, event.orderId, eventSerializer.serialize(event),
    )
}
