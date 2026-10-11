package com.example.saga.shipping.adapter.`in`.messaging.mapper

import com.example.saga.infrastructure.messaging.serialization.UnprocessableMessageException
import com.example.saga.order.contract.event.OrderCompletedEvent
import com.example.saga.shipping.adapter.`in`.requireV7
import com.example.saga.shipping.application.command.RegisterShipmentCommand
import java.util.UUID
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

@Component
class OrderCompletedMapper(private val jsonMapper: JsonMapper) {
    fun toCommand(payload: String): RegisterShipmentCommand = try {
        val event = jsonMapper.readValue(payload, OrderCompletedEvent::class.java)
        RegisterShipmentCommand(UUID.fromString(event.orderId).requireV7("orderId"))
    } catch (e: JacksonException) {
        throw UnprocessableMessageException("이벤트 본문을 해석할 수 없습니다.", e)
    } catch (e: IllegalArgumentException) {
        throw UnprocessableMessageException("이벤트의 ID 형식이 유효하지 않습니다.", e)
    }
}
