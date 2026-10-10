package com.example.saga.payment.adapter.`in`.web.request

import com.example.saga.payment.adapter.`in`.requireV7
import com.example.saga.payment.application.port.`in`.command.PayCommand
import java.util.UUID

data class PayRequest(
    val orderId: UUID,
    val amount: Long,
) {
    fun toCommand(): PayCommand = PayCommand(
        orderId = orderId.requireV7("orderId"),
        amount = amount,
    )
}
