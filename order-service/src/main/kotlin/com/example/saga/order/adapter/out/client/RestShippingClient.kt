package com.example.saga.order.adapter.out.client

import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.domain.Order
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class RestShippingClient(
    @Qualifier("shippingInstances") private val instances: ParticipantInstances,
    private val participantCaller: ParticipantCaller,
) : ShippingClient {

    override fun cancelShipment(order: Order): String? = participantCaller.post(
        instances = instances,
        path = "/order-shipments/{orderId}/cancel",
        orderId = order.id.toString(),
        body = null,
        rejectionCode = "SHIPMENT_CANCEL_REJECTED",
    )
}
