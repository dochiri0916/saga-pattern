package com.example.saga.shipping.adapter.`in`.messaging

import com.example.saga.shipping.adapter.`in`.requireV7
import com.example.saga.shipping.application.port.`in`.command.RegisterShipmentCommand
import java.util.UUID

data class OrderCompletedMessage(
    val orderId: UUID,
) {
    fun toCommand(eventId: UUID): RegisterShipmentCommand = RegisterShipmentCommand(
        eventId = eventId,
        orderId = orderId.requireV7("orderId"),
    )

    companion object {
        const val EVENT_TYPE = "ORDER_COMPLETED"
    }
}
