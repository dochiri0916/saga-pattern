package com.example.saga.shipping.adapter.`in`.messaging

import com.example.saga.shipping.adapter.`in`.InvalidUuidV7Exception
import com.example.saga.shipping.application.port.`in`.usecase.RegisterShipmentUseCase
import com.example.saga.shipping.domain.exception.ShipmentDomainException
import com.example.saga.platform.consumer.MessageReader
import com.example.saga.platform.consumer.UnprocessableMessageException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(OrderCompletedListener::class.java)

@Component
class OrderCompletedListener(
    private val registerShipmentUseCase: RegisterShipmentUseCase,
    private val messageReader: MessageReader,
) {

    @KafkaListener(topics = ["\${shipping.kafka.topics.order-completed.name}"])
    fun onOrderEvent(record: ConsumerRecord<String, String>) {
        val type = messageReader.eventType(record)
        if (type != OrderCompletedMessage.EVENT_TYPE) {
            log.debug("처리 대상이 아닌 이벤트라서 건너뜁니다. type={}, {}", type, messageReader.position(record))
            return
        }
        try {
            val message = messageReader.body(record, OrderCompletedMessage::class.java)
            registerShipmentUseCase.register(message.toCommand(messageReader.eventId(record)))
        } catch (e: InvalidUuidV7Exception) {
            throw UnprocessableMessageException("이벤트의 내용이 유효하지 않습니다. ${messageReader.position(record)}", e)
        } catch (e: ShipmentDomainException) {
            throw UnprocessableMessageException("이벤트의 내용이 유효하지 않습니다. ${messageReader.position(record)}", e)
        }
    }
}
