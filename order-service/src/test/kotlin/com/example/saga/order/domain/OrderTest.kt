package com.example.saga.order.domain

import com.example.saga.order.domain.exception.InvalidOrderItemStatusTransitionException
import com.example.saga.order.domain.exception.OrderItemChangeNotAllowedException
import com.example.saga.order.domain.exception.OrderItemNotFoundException
import com.example.saga.order.domain.vo.OrderId
import com.example.saga.order.domain.vo.OrderItemId
import com.example.saga.order.domain.vo.OrderItemStatus
import com.example.saga.order.domain.vo.OrderQuantity
import com.example.saga.order.domain.vo.OrderStatus
import com.example.saga.order.domain.vo.StockReferenceId
import com.example.saga.order.domain.vo.UnitPrice
import com.github.f4b6a3.uuid.UuidCreator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OrderTest {

    private fun item(): OrderItem = OrderItem.register(
        OrderItemId.of(UuidCreator.getTimeOrderedEpoch()),
        StockReferenceId.of(UuidCreator.getTimeOrderedEpoch()),
        OrderQuantity.of(1),
        UnitPrice.of(1000),
    )

    private val first = item()
    private val second = item()
    private val completed = Order.register(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderItems.of(listOf(first, second))).complete()

    @Test
    fun `완료된 주문의 항목 하나를 취소하면 그 항목만 취소되고 주문은 유지된다`() {
        val order = completed.cancelItem(first.id)

        assertEquals(OrderItemStatus.CANCELLED, order.item(first.id).status)
        assertEquals(OrderItemStatus.ORDERED, order.item(second.id).status)
        assertEquals(OrderStatus.COMPLETED, order.status)
        assertEquals(completed.totalAmount, order.totalAmount)
    }

    @Test
    fun `모든 항목이 취소되면 주문도 취소되고 반품이 섞이면 주문은 완료로 남는다`() {
        assertEquals(OrderStatus.CANCELLED, completed.cancelItem(first.id).cancelItem(second.id).status)
        assertEquals(OrderStatus.COMPLETED, completed.cancelItem(first.id).returnItem(second.id).status)
    }

    @Test
    fun `완료되지 않은 주문의 항목은 바꿀 수 없다`() {
        val pending = Order.register(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderItems.of(listOf(first)))

        assertThrows<OrderItemChangeNotAllowedException> { pending.cancelItem(first.id) }
    }

    @Test
    fun `이미 취소된 항목은 다시 바꿀 수 없다`() {
        val order = completed.cancelItem(first.id)

        assertThrows<InvalidOrderItemStatusTransitionException> { order.returnItem(first.id) }
    }

    @Test
    fun `없는 항목은 바꿀 수 없다`() {
        assertThrows<OrderItemNotFoundException> { completed.cancelItem(OrderItemId.of(UuidCreator.getTimeOrderedEpoch())) }
    }
}
