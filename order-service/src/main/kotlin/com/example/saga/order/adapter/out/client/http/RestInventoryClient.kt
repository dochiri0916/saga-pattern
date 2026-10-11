package com.example.saga.order.adapter.out.client.http

import com.example.saga.order.adapter.out.client.mapper.ParticipantCommandMapper
import com.example.saga.order.application.port.out.InventoryClient
import com.example.saga.order.domain.order.Order
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class RestInventoryClient(
    @Qualifier("inventoryInstances") private val instances: ParticipantInstances,
    private val participantCaller: ParticipantCaller,
) : InventoryClient {

    override fun decreaseStock(order: Order): String? = participantCaller.post(
        instances = instances,
        path = "/order-stocks/{orderId}/decrease",
        orderId = order.id.toString(),
        body = ParticipantCommandMapper.decrease(order),
        rejectionCode = "STOCK_DECREASE_REJECTED",
    )

    override fun restoreStock(order: Order) {
        participantCaller.post(
            instances = instances,
            path = "/order-stocks/{orderId}/restore",
            orderId = order.id.toString(),
            body = ParticipantCommandMapper.restore(order),
        )
    }
}
