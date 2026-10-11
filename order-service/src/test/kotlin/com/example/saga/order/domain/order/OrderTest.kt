package com.example.saga.order.domain.order

import com.example.saga.order.domain.order.event.OrderCancelled
import com.example.saga.order.domain.order.event.OrderEvent
import com.example.saga.order.domain.order.event.OrderItemCancelled
import com.example.saga.order.domain.order.exception.InvalidOrderItemStatusTransitionException
import com.example.saga.order.domain.order.exception.OrderItemChangeNotAllowedException
import com.example.saga.order.domain.order.exception.OrderItemNotFoundException
import com.example.saga.order.domain.order.vo.OrderAmount
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.OrderItemId
import com.example.saga.order.domain.order.vo.OrderItemStatus
import com.example.saga.order.domain.order.vo.OrderQuantity
import com.example.saga.order.domain.order.vo.OrderStatus
import com.example.saga.order.domain.order.vo.StockReferenceId
import com.example.saga.order.domain.order.vo.UnitPrice
import com.github.f4b6a3.uuid.UuidCreator
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderTest {

    private fun item(): OrderItem = OrderItem.register(
        OrderItemId.of(UuidCreator.getTimeOrderedEpoch()),
        StockReferenceId.of(UuidCreator.getTimeOrderedEpoch()),
        OrderQuantity.of(1),
        UnitPrice.of(1000),
    )

    private val first = item()
    private val second = item()
    private val completed = Order.register(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderItems.of(listOf(first, second))).complete().order

    @Test
    fun `완료된 주문의 항목 하나를 취소하면 그 항목만 취소되고 주문은 유지된다`() {
        val order = completed.cancelItem(first.id).order

        assertEquals(OrderItemStatus.CANCELLED, order.item(first.id).status)
        assertEquals(OrderItemStatus.ORDERED, order.item(second.id).status)
        assertEquals(OrderStatus.COMPLETED, order.status)
        assertEquals(completed.totalAmount, order.totalAmount)
    }

    @Test
    fun `모든 항목이 취소되면 주문도 취소되고 반품이 섞이면 주문은 완료로 남는다`() {
        assertEquals(OrderStatus.CANCELLED, completed.cancelItem(first.id).order.cancelItem(second.id).order.status)
        assertEquals(OrderStatus.COMPLETED, completed.cancelItem(first.id).order.returnItem(second.id).order.status)
    }

    @Test
    fun `상태가 바뀌면 그 변경이 낸 이벤트를 함께 돌려준다`() {
        val itemCancelled = completed.cancelItem(first.id)
        val allCancelled = itemCancelled.order.cancelItem(second.id)

        assertEquals(listOf<OrderEvent>(OrderItemCancelled(completed.id, first.id)), itemCancelled.events.asList())
        assertEquals(
            listOf(OrderItemCancelled(completed.id, second.id), OrderCancelled(completed.id)),
            allCancelled.events.asList(),
        )
    }

    @Test
    fun `주문을 취소하면 남은 항목도 취소되고 반품된 항목은 그대로 남는다`() {
        val cancelled = completed.returnItem(first.id).order.cancel().order

        assertEquals(OrderStatus.CANCELLED, cancelled.status)
        assertEquals(OrderItemStatus.RETURNED, cancelled.item(first.id).status)
        assertEquals(OrderItemStatus.CANCELLED, cancelled.item(second.id).status)
        assertEquals(OrderAmount.of(0), cancelled.remainingAmount)
        assertEquals(completed.totalAmount, cancelled.closedAmount)
    }

    @Test
    fun `완료되지 않은 주문의 항목은 바꿀 수 없다`() {
        val pending = Order.register(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderItems.of(listOf(first)))

        assertThrows<OrderItemChangeNotAllowedException> { pending.cancelItem(first.id) }
    }

    @Test
    fun `이미 취소된 항목은 다시 바꿀 수 없다`() {
        val order = completed.cancelItem(first.id).order

        assertThrows<InvalidOrderItemStatusTransitionException> { order.returnItem(first.id) }
    }

    @Test
    fun `없는 항목은 바꿀 수 없다`() {
        assertThrows<OrderItemNotFoundException> { completed.cancelItem(OrderItemId.of(UuidCreator.getTimeOrderedEpoch())) }
    }
}
