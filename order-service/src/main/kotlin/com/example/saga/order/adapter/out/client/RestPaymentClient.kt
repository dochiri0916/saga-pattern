package com.example.saga.order.adapter.out.client

import com.example.saga.order.application.port.out.PaymentClient
import com.example.saga.order.domain.Order
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class RestPaymentClient(
    @Qualifier("paymentInstances") private val instances: ParticipantInstances,
    private val participantCaller: ParticipantCaller,
) : PaymentClient {

    override fun pay(order: Order): String? = participantCaller.post(
        instances = instances,
        path = "/payments",
        orderId = order.id.toString(),
        body = PayRequest(order.id.toString(), order.totalAmount.value),
        rejectionCode = "PAYMENT_REJECTED",
    )

    override fun cancelPayment(order: Order) {
        participantCaller.post(
            instances = instances,
            path = "/order-payments/{orderId}/cancel",
            orderId = order.id.toString(),
            body = null,
        )
    }
}

data class PayRequest(
    val orderId: String,
    val amount: Long,
)
