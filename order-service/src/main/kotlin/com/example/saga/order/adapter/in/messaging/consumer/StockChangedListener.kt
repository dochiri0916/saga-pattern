package com.example.saga.order.adapter.`in`.messaging.consumer

import com.example.saga.infrastructure.messaging.inbox.InboxGuard
import com.example.saga.infrastructure.messaging.serialization.EnvelopeDeserializer
import com.example.saga.infrastructure.messaging.serialization.UnprocessableMessageException
import com.example.saga.inventory.contract.event.StockChangedEvent
import com.example.saga.order.adapter.`in`.messaging.mapper.StockChangedMapper
import com.example.saga.order.application.port.`in`.UpdateStockSnapshotUseCase
import com.example.saga.order.domain.order.exception.OrderDomainException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(StockChangedListener::class.java)

@Component
class StockChangedListener(
    private val updateStockSnapshotUseCase: UpdateStockSnapshotUseCase,
    private val messageReader: EnvelopeDeserializer,
    private val mapper: StockChangedMapper,
    private val inboxGuard: InboxGuard,
) {

    @KafkaListener(topics = ["\${order.kafka.topics.stock-changed.name}"])
    fun onStockEvent(record: ConsumerRecord<String, String>) {
        val envelope = messageReader.deserialize(record)
        if (envelope.eventType != StockChangedEvent.EVENT_TYPE) {
            log.debug("처리 대상이 아닌 이벤트라서 건너뜁니다. type={}, {}", envelope.eventType, envelope.position)
            return
        }
        try {
            inboxGuard.execute(envelope.eventId) { updateStockSnapshotUseCase.update(mapper.toCommand(envelope.payload)) }
        } catch (e: OrderDomainException) {
            throw UnprocessableMessageException("이벤트의 내용이 유효하지 않습니다. ${envelope.position}", e)
        }
    }
}
