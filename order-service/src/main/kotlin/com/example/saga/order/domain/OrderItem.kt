package com.example.saga.order.domain

import com.example.saga.order.domain.exception.InvalidOrderItemStatusTransitionException
import com.example.saga.order.domain.vo.OrderAmount
import com.example.saga.order.domain.vo.OrderItemId
import com.example.saga.order.domain.vo.OrderItemStatus
import com.example.saga.order.domain.vo.OrderQuantity
import com.example.saga.order.domain.vo.StockReferenceId
import com.example.saga.order.domain.vo.UnitPrice

class OrderItem private constructor(
    val id: OrderItemId,
    val stockReferenceId: StockReferenceId,
    val quantity: OrderQuantity,
    val unitPrice: UnitPrice,
    val status: OrderItemStatus,
) {

    val amount: OrderAmount
        get() = unitPrice * quantity

    fun cancel(): OrderItem = transitionTo(OrderItemStatus.CANCELLED)

    fun markReturned(): OrderItem = transitionTo(OrderItemStatus.RETURNED)

    private fun transitionTo(target: OrderItemStatus): OrderItem {
        if (!status.canTransitionTo(target)) throw InvalidOrderItemStatusTransitionException(status, target)
        return OrderItem(id, stockReferenceId, quantity, unitPrice, target)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "OrderItem(id=$id, stock=$stockReferenceId, quantity=$quantity, status=$status)"

    companion object {
        fun register(id: OrderItemId, stockReferenceId: StockReferenceId, quantity: OrderQuantity, unitPrice: UnitPrice): OrderItem =
            OrderItem(id, stockReferenceId, quantity, unitPrice, OrderItemStatus.ORDERED)

        fun restore(
            id: OrderItemId,
            stockReferenceId: StockReferenceId,
            quantity: OrderQuantity,
            unitPrice: UnitPrice,
            status: OrderItemStatus,
        ): OrderItem = OrderItem(id, stockReferenceId, quantity, unitPrice, status)
    }
}
