package com.example.saga.order.adapter.out.client.http

import com.example.saga.order.adapter.out.client.mapper.ParticipantCommandMapper
import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.domain.order.Order
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
        orderId = ParticipantCommandMapper.cancelShipment(order).orderId,
        body = null,
        rejectionCode = "SHIPMENT_CANCEL_REJECTED",
    )
}
