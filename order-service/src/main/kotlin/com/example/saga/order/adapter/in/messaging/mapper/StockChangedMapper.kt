package com.example.saga.order.adapter.`in`.messaging.mapper

import com.example.saga.infrastructure.messaging.serialization.UnprocessableMessageException
import com.example.saga.inventory.contract.event.StockChangedEvent
import com.example.saga.order.adapter.`in`.requireV7
import com.example.saga.order.application.command.UpdateStockSnapshotCommand
import java.util.UUID
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper

@Component
class StockChangedMapper(private val jsonMapper: JsonMapper) {
    fun toCommand(payload: String): UpdateStockSnapshotCommand = try {
        val event = jsonMapper.readValue(payload, StockChangedEvent::class.java)
        UpdateStockSnapshotCommand(UUID.fromString(event.stockId).requireV7("stockId"), event.name, event.price, event.quantity, event.version)
    } catch (e: JacksonException) {
        throw UnprocessableMessageException("이벤트 본문을 해석할 수 없습니다.", e)
    } catch (e: IllegalArgumentException) {
        throw UnprocessableMessageException("이벤트의 ID 형식이 유효하지 않습니다.", e)
    }
}
