package com.example.saga.order.domain

import com.example.saga.order.domain.event.OrderCancelled
import com.example.saga.order.domain.event.OrderCompleted
import com.example.saga.order.domain.event.OrderEvent
import com.example.saga.order.domain.event.OrderItemCancelled
import com.example.saga.order.domain.event.OrderItemReturned
import com.example.saga.order.domain.exception.InvalidOrderStatusTransitionException
import com.example.saga.order.domain.exception.OrderItemChangeNotAllowedException
import com.example.saga.order.domain.vo.OrderAmount
import com.example.saga.order.domain.vo.OrderId
import com.example.saga.order.domain.vo.OrderItemId
import com.example.saga.order.domain.vo.OrderStatus

class Order private constructor(
    val id: OrderId,
    private val orderItems: OrderItems,
    val status: OrderStatus,

    val events: List<OrderEvent> = emptyList(),
) {

    val items: List<OrderItem>
        get() = orderItems.asList()

    val totalAmount: OrderAmount
        get() = orderItems.totalAmount

    val remainingAmount: OrderAmount
        get() = orderItems.remainingAmount

    val closedAmount: OrderAmount
        get() = orderItems.closedAmount

    fun item(itemId: OrderItemId): OrderItem = orderItems.get(itemId)

    fun complete(): Order = transitionTo(OrderStatus.COMPLETED, OrderCompleted(id))

    fun cancel(): Order = transitionTo(OrderStatus.CANCELLED, OrderCancelled(id))

    fun cancelItem(itemId: OrderItemId): Order = changeItem(itemId, OrderItem::cancel, OrderItemCancelled(id, itemId))

    fun returnItem(itemId: OrderItemId): Order = changeItem(itemId, OrderItem::markReturned, OrderItemReturned(id, itemId))

    private fun changeItem(itemId: OrderItemId, change: (OrderItem) -> OrderItem, event: OrderEvent): Order {
        if (status != OrderStatus.COMPLETED) throw OrderItemChangeNotAllowedException(status)
        val changedItems = orderItems.replace(change(orderItems.get(itemId)))
        if (!changedItems.isAllCancelled()) return Order(id, changedItems, status, listOf(event))
        return Order(id, changedItems, status).cancel().let { Order(id, changedItems, it.status, listOf(event) + it.events) }
    }

    private fun transitionTo(target: OrderStatus, event: OrderEvent): Order {
        if (!status.canTransitionTo(target)) throw InvalidOrderStatusTransitionException(status, target)
        return Order(id, orderItems, target, listOf(event))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Order(id=$id, status=$status, items=$orderItems)"

    companion object {
        fun register(id: OrderId, items: OrderItems): Order = Order(id, items, OrderStatus.PENDING)

        fun restore(id: OrderId, items: OrderItems, status: OrderStatus): Order = Order(id, items, status)
    }
}
