package com.example.saga.order.domain.order

import com.example.saga.order.domain.order.event.OrderCancelled
import com.example.saga.order.domain.order.event.OrderCompleted
import com.example.saga.order.domain.order.event.OrderEvent
import com.example.saga.order.domain.order.event.OrderEvents
import com.example.saga.order.domain.order.event.OrderItemCancelled
import com.example.saga.order.domain.order.event.OrderItemReturned
import com.example.saga.order.domain.order.exception.InvalidOrderStatusTransitionException
import com.example.saga.order.domain.order.exception.OrderItemChangeNotAllowedException
import com.example.saga.order.domain.order.vo.OrderAmount
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.OrderItemId
import com.example.saga.order.domain.order.vo.OrderStatus

class Order private constructor(
    val id: OrderId,
    private val orderItems: OrderItems,
    val status: OrderStatus,
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

    fun complete(): OrderChange = transitionTo(OrderStatus.COMPLETED, orderItems, OrderCompleted(id))

    fun cancel(): OrderChange = transitionTo(OrderStatus.CANCELLED, orderItems.cancelRemaining(), OrderCancelled(id))

    fun cancelItem(itemId: OrderItemId): OrderChange = changeItem(itemId, OrderItem::cancel, OrderItemCancelled(id, itemId))

    fun returnItem(itemId: OrderItemId): OrderChange = changeItem(itemId, OrderItem::markReturned, OrderItemReturned(id, itemId))

    private fun changeItem(itemId: OrderItemId, change: (OrderItem) -> OrderItem, event: OrderEvent): OrderChange {
        if (status != OrderStatus.COMPLETED) throw OrderItemChangeNotAllowedException(status)
        val changed = Order(id, orderItems.replace(change(orderItems.get(itemId))), status)
        val itemChanged = OrderEvents.of(listOf(event))
        if (!changed.orderItems.isAllCancelled()) return OrderChange.of(changed, itemChanged)
        val cancelled = changed.cancel()
        return OrderChange.of(cancelled.order, itemChanged + cancelled.events)
    }

    private fun transitionTo(target: OrderStatus, items: OrderItems, event: OrderEvent): OrderChange {
        if (!status.canTransitionTo(target)) throw InvalidOrderStatusTransitionException(status, target)
        return OrderChange.of(Order(id, items, target), OrderEvents.of(listOf(event)))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Order(id=$id, status=$status, items=$orderItems)"

    companion object {
        fun register(id: OrderId, orderItems: OrderItems): Order = Order(id, orderItems, OrderStatus.PENDING)

        fun restore(id: OrderId, orderItems: OrderItems, status: OrderStatus): Order = Order(id, orderItems, status)
    }
}
