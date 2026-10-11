package com.example.saga.order.application.integration.mapper

import com.example.saga.order.contract.event.OrderCompletedEvent
import com.example.saga.order.contract.event.OrderCompletedItem
import com.example.saga.order.domain.order.OrderChange
import com.example.saga.order.domain.order.event.OrderCancelled
import com.example.saga.order.domain.order.event.OrderCompleted
import com.example.saga.order.domain.order.event.OrderItemCancelled
import com.example.saga.order.domain.order.event.OrderItemReturned

object OrderIntegrationEventMapper {
    fun map(change: OrderChange): List<OrderCompletedEvent> = change.events.asList().mapNotNull { event ->
        when (event) {
            is OrderCompleted -> OrderCompletedEvent(
                change.order.id.toString(), change.order.totalAmount.value,
                change.order.items.map { OrderCompletedItem(it.stockReferenceId.toString(), it.quantity.value) },
            )
            is OrderCancelled, is OrderItemCancelled, is OrderItemReturned -> null
        }
    }
}
