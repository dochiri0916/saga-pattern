package com.example.saga.shipping.adapter.`in`.messaging.consumer

import com.example.saga.infrastructure.messaging.inbox.InboxGuard
import com.example.saga.infrastructure.messaging.serialization.EnvelopeDeserializer
import com.example.saga.infrastructure.messaging.serialization.UnprocessableMessageException
import com.example.saga.order.contract.event.OrderCompletedEvent
import com.example.saga.shipping.adapter.`in`.messaging.mapper.OrderCompletedMapper
import com.example.saga.shipping.application.port.`in`.RegisterShipmentUseCase
import com.example.saga.shipping.domain.shipment.exception.ShipmentDomainException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(OrderCompletedListener::class.java)

@Component
class OrderCompletedListener(
    private val registerShipmentUseCase: RegisterShipmentUseCase,
    private val messageReader: EnvelopeDeserializer,
    private val mapper: OrderCompletedMapper,
    private val inboxGuard: InboxGuard,
) {

    @KafkaListener(topics = ["\${shipping.kafka.topics.order-completed.name}"])
    fun onOrderEvent(record: ConsumerRecord<String, String>) {
        val envelope = messageReader.deserialize(record)
        if (envelope.eventType != OrderCompletedEvent.EVENT_TYPE) {
            log.debug("처리 대상이 아닌 이벤트라서 건너뜁니다. type={}, {}", envelope.eventType, envelope.position)
            return
        }
        try {
            inboxGuard.execute(envelope.eventId) { registerShipmentUseCase.register(mapper.toCommand(envelope.payload)) }
        } catch (e: ShipmentDomainException) {
            throw UnprocessableMessageException("이벤트의 내용이 유효하지 않습니다. ${envelope.position}", e)
        }
    }
}
