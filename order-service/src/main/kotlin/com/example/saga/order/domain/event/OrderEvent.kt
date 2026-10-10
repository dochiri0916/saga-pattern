package com.example.saga.order.domain.event

import com.example.saga.order.domain.vo.OrderId
import com.example.saga.order.domain.vo.OrderItemId

sealed interface OrderEvent {
    val orderId: OrderId
}

data class OrderCompleted(override val orderId: OrderId) : OrderEvent

data class OrderCancelled(override val orderId: OrderId) : OrderEvent

data class OrderItemCancelled(override val orderId: OrderId, val itemId: OrderItemId) : OrderEvent

data class OrderItemReturned(override val orderId: OrderId, val itemId: OrderItemId) : OrderEvent
