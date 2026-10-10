package com.example.saga.order.adapter.`in`.messaging

import com.example.saga.order.adapter.`in`.InvalidUuidV7Exception
import com.example.saga.order.application.port.`in`.usecase.UpdateStockSnapshotUseCase
import com.example.saga.order.domain.exception.OrderDomainException
import com.example.saga.platform.consumer.MessageReader
import com.example.saga.platform.consumer.UnprocessableMessageException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(StockChangedListener::class.java)

@Component
class StockChangedListener(
    private val updateStockSnapshotUseCase: UpdateStockSnapshotUseCase,
    private val messageReader: MessageReader,
) {

    @KafkaListener(topics = ["\${order.kafka.topics.stock-changed.name}"])
    fun onStockEvent(record: ConsumerRecord<String, String>) {
        val type = messageReader.eventType(record)
        if (type != StockChangedMessage.EVENT_TYPE) {
            log.debug("처리 대상이 아닌 이벤트라서 건너뜁니다. type={}, {}", type, messageReader.position(record))
            return
        }
        try {
            updateStockSnapshotUseCase.update(messageReader.body(record, StockChangedMessage::class.java).toCommand())
        } catch (e: InvalidUuidV7Exception) {
            throw UnprocessableMessageException("이벤트의 내용이 유효하지 않습니다. ${messageReader.position(record)}", e)
        } catch (e: OrderDomainException) {
            throw UnprocessableMessageException("이벤트의 내용이 유효하지 않습니다. ${messageReader.position(record)}", e)
        }
    }
}
