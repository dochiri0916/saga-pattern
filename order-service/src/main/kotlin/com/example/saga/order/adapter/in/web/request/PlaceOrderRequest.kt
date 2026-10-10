package com.example.saga.order.adapter.`in`.web.request

import com.example.saga.order.adapter.`in`.requireV7
import com.example.saga.order.application.port.`in`.command.PlaceOrderCommand
import com.example.saga.order.application.port.`in`.command.PlaceOrderItemCommand
import java.util.UUID

data class PlaceOrderRequest(
    val items: List<PlaceOrderItemRequest>,
) {
    fun toCommand(idempotencyKey: String?): PlaceOrderCommand = PlaceOrderCommand(
        items = items.map { PlaceOrderItemCommand(stockId = it.stockId.requireV7("stockId"), quantity = it.quantity) },
        idempotencyKey = idempotencyKey?.also { if (it.isBlank() || it.length > MAX_IDEMPOTENCY_KEY_LENGTH) throw InvalidIdempotencyKeyException() },
    )

    private companion object {
        const val MAX_IDEMPOTENCY_KEY_LENGTH = 64
    }
}

class InvalidIdempotencyKeyException : IllegalArgumentException("Idempotency-Key는 비어 있지 않은 64자 이하의 문자열이어야 합니다.")

data class PlaceOrderItemRequest(
    val stockId: UUID,
    val quantity: Int,
)
