package com.example.saga.order.domain.order

import com.example.saga.order.domain.order.event.OrderEvents

class OrderChange private constructor(
    val order: Order,
    val events: OrderEvents,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderChange) return false
        return order == other.order && events == other.events
    }

    override fun hashCode(): Int = 31 * order.hashCode() + events.hashCode()

    override fun toString(): String = "OrderChange(order=$order, events=$events)"

    companion object {
        fun of(order: Order, events: OrderEvents): OrderChange = OrderChange(order, events)
    }
}
