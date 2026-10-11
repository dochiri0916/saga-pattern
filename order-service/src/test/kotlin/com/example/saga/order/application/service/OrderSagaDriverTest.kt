package com.example.saga.order.application.service

import com.example.saga.order.application.exception.OrderCancelPeriodExpiredException
import com.example.saga.order.application.port.out.ParticipantCallException
import com.example.saga.order.application.port.out.ParticipantUnavailableException
import com.example.saga.order.application.saga.model.OrderSaga
import com.example.saga.order.application.saga.model.OrderSagaStatus
import com.example.saga.order.application.saga.orchestrator.OrderSagaDriver
import com.example.saga.order.application.saga.orchestrator.OrderSagaStepExecutor
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.OrderItem
import com.example.saga.order.domain.order.OrderItems
import com.example.saga.order.domain.order.policy.OrderCancelPolicy
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.OrderItemId
import com.example.saga.order.domain.order.vo.OrderQuantity
import com.example.saga.order.domain.order.vo.OrderStatus
import com.example.saga.order.domain.order.vo.StockReferenceId
import com.example.saga.order.domain.order.vo.UnitPrice
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderSagaDriverTest {

    private val clock = MutableClock(Instant.parse("2026-10-10T00:00:00Z"))
    private val sagas = InMemoryOrderSagaRepository()
    private val orders = InMemoryOrderRepository()
    private val events = RecordingEventPublicationPort()
    private val participants = FakeParticipants()
    private val executor = OrderSagaStepExecutor(sagas, orders, events, OrderCancelPolicy(Duration.ofDays(7)), clock, RecordingAuditTrail())
    private val driver = OrderSagaDriver(executor, participants, participants, participants)

    private val order = Order.register(
        OrderId.of(UuidCreator.getTimeOrderedEpoch()),
        OrderItems.of(listOf(OrderItem.register(OrderItemId.of(UuidCreator.getTimeOrderedEpoch()), StockReferenceId.of(UuidCreator.getTimeOrderedEpoch()), OrderQuantity.of(2), UnitPrice.of(1000)))),
    )

    private fun place(): OrderSaga = driver.drive(order, executor.start(order))

    private fun cancel(): OrderSaga = driver.drive(order, executor.cancel(order.id))

    private fun orderStatus(): OrderStatus = orders.orders.getValue(order.id).status

    @Test
    fun `재고 차감과 결제가 되면 주문이 완료되고 확정 이벤트가 한 번 나간다`() {
        val saga = place()

        assertEquals(OrderSagaStatus.COMPLETED, saga.status)
        assertEquals(OrderStatus.COMPLETED, orderStatus())
        assertEquals(listOf("decrease", "pay"), participants.calls)
        assertEquals(listOf(order.id), events.completed)
    }

    @Test
    fun `재고 차감이 거절되면 결제하지 않고 되돌릴 것 없이 취소한다`() {
        participants.decrease = { "INSUFFICIENT_STOCK" }

        val saga = place()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals("INSUFFICIENT_STOCK", saga.failureReason)
        assertEquals(OrderStatus.CANCELLED, orderStatus())
        assertEquals(listOf("decrease"), participants.calls)
        assertEquals(emptyList(), events.completed)
    }

    @Test
    fun `결제가 거절되면 재고를 되돌리고 취소한다`() {
        participants.pay = { "GATEWAY_DECLINED" }

        val saga = place()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals("GATEWAY_DECLINED", saga.failureReason)
        assertEquals(listOf("decrease", "pay", "restore"), participants.calls)
        assertEquals(emptyList(), events.completed)
    }

    @Test
    fun `재고 차감의 결과를 알 수 없으면 차감됐다고 보고 되돌린다`() {
        participants.decrease = { throw ParticipantCallException("응답 없음") }

        val saga = place()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals("CALL_FAILED", saga.failureReason)
        assertEquals(listOf("decrease", "restore"), participants.calls)
    }

    @Test
    fun `결제의 결과를 알 수 없으면 결제를 취소하고 재고를 되돌린다`() {
        participants.pay = { throw ParticipantCallException("응답 없음") }

        val saga = place()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals(listOf("decrease", "pay", "cancelPayment", "restore"), participants.calls)
        assertEquals(OrderStatus.CANCELLED, orderStatus())
    }

    @Test
    fun `되돌리는 호출의 결과를 알 수 없으면 포기하지 않고 그 상태로 남긴다`() {
        participants.pay = { "GATEWAY_DECLINED" }
        participants.restore = { throw ParticipantCallException("응답 없음") }

        val saga = place()

        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, saga.status)
        assertEquals(OrderStatus.PENDING, orderStatus())
    }

    @Test
    fun `복구 작업은 남은 보상을 다시 시도해 끝낸다`() {
        participants.pay = { "GATEWAY_DECLINED" }
        participants.restore = { throw ParticipantCallException("응답 없음") }
        place()
        participants.restore = { }
        clock.advance(Duration.ofSeconds(11))

        val claimed = executor.claimStalled(clock.instant().minusSeconds(10), 100)
        val recovered = driver.drive(order, claimed.single())

        assertEquals(OrderSagaStatus.CANCELLED, recovered.status)
        assertEquals("GATEWAY_DECLINED", recovered.failureReason)
        assertEquals(OrderStatus.CANCELLED, orderStatus())
    }

    @Test
    fun `복구 작업은 도중에 멈춘 진행 중 사가를 완료시키지 않고 중단해 되돌린다`() {
        val started = executor.start(order)
        executor.apply(order.id, started.status, com.example.saga.order.application.saga.model.OrderSagaReply.STOCK_DECREASED, null)
        clock.advance(Duration.ofSeconds(11))

        val claimed = executor.claimStalled(clock.instant().minusSeconds(10), 100).single()
        assertEquals(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, claimed.status)
        assertEquals("TIMED_OUT", claimed.failureReason)

        val recovered = driver.drive(order, claimed)
        assertEquals(OrderSagaStatus.CANCELLED, recovered.status)
        assertEquals(listOf("cancelPayment", "restore"), participants.calls)
    }

    @Test
    fun `복구 작업은 방금 상태가 바뀐 사가와 끝난 사가를 집지 않는다`() {
        place()
        val pending = Order.register(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderItems.of(order.items))
        clock.advance(Duration.ofSeconds(11))
        executor.start(pending)

        assertEquals(emptyList(), executor.claimStalled(clock.instant().minusSeconds(10), 100))
    }

    @Test
    fun `완료된 주문을 취소하면 배송 취소, 결제 취소, 재고 복원 순으로 되돌린다`() {
        place()
        participants.calls.clear()

        val saga = cancel()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals("CANCEL_REQUESTED", saga.failureReason)
        assertEquals(listOf("cancelShipment", "cancelPayment", "restore"), participants.calls)
        assertEquals(OrderStatus.CANCELLED, orderStatus())
    }

    @Test
    fun `배송 취소가 거절되면 아무것도 되돌리지 않고 주문은 완료인 채로 남는다`() {
        place()
        participants.calls.clear()
        participants.cancelShipment = { "ALREADY_SHIPPED" }

        val saga = cancel()

        assertEquals(OrderSagaStatus.COMPLETED, saga.status)
        assertEquals("ALREADY_SHIPPED", saga.failureReason)
        assertEquals(listOf("cancelShipment"), participants.calls)
        assertEquals(OrderStatus.COMPLETED, orderStatus())
        assertEquals(listOf(order.id), events.completed, "완료로 돌아와도 확정 이벤트를 다시 내보내지 않는다")
    }

    @Test
    fun `배송 취소의 결과를 알 수 없으면 결제와 재고를 건드리지 않고 답을 받을 때까지 그 상태로 남긴다`() {
        place()
        participants.calls.clear()
        participants.cancelShipment = { throw ParticipantCallException("응답 없음") }

        val saga = cancel()

        assertEquals(OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED, saga.status)
        assertEquals(listOf("cancelShipment"), participants.calls)
        assertEquals(OrderStatus.COMPLETED, orderStatus())

        participants.cancelShipment = { null }
        assertEquals(OrderSagaStatus.CANCELLED, cancel().status)
    }

    @Test
    fun `이미 취소된 주문을 다시 취소해도 아무것도 부르지 않는다`() {
        participants.decrease = { "INSUFFICIENT_STOCK" }
        place()
        participants.calls.clear()

        assertEquals(OrderSagaStatus.CANCELLED, cancel().status)
        assertEquals(emptyList(), participants.calls)
    }

    @Test
    fun `그 사이 다른 쪽이 사가를 옮겼으면 늦게 온 결과를 반영하지 않는다`() {
        val started = executor.start(order)
        executor.abort(order.id, com.example.saga.order.application.saga.model.OrderSagaAbortReason.TIMED_OUT)

        val applied = executor.apply(order.id, started.status, com.example.saga.order.application.saga.model.OrderSagaReply.STOCK_DECREASED, null)

        assertEquals(null, applied)
        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, sagas.sagas.getValue(order.id).status)
    }

    @Test
    fun `재고 서비스에 요청을 보내지 못하면 되돌릴 것 없이 취소한다`() {
        participants.decrease = { throw ParticipantUnavailableException("회로 열림") }

        val saga = place()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals("INVENTORY_UNAVAILABLE", saga.failureReason)
        assertEquals(listOf("decrease"), participants.calls)
        assertEquals(OrderStatus.CANCELLED, orderStatus())
    }

    @Test
    fun `결제 서비스에 요청을 보내지 못하면 결제 취소 없이 재고만 되돌린다`() {
        participants.pay = { throw ParticipantUnavailableException("회로 열림") }

        val saga = place()

        assertEquals(OrderSagaStatus.CANCELLED, saga.status)
        assertEquals("PAYMENT_UNAVAILABLE", saga.failureReason)
        assertEquals(listOf("decrease", "pay", "restore"), participants.calls)
    }

    @Test
    fun `되돌리는 요청을 보내지 못하면 포기하지 않고 그 상태로 남긴다`() {
        participants.pay = { "GATEWAY_DECLINED" }
        participants.restore = { throw ParticipantUnavailableException("회로 열림") }

        val saga = place()

        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, saga.status)
        assertEquals("GATEWAY_DECLINED", saga.failureReason)
    }

    @Test
    fun `취소할 수 있는 기간이 지난 완료 주문은 아무것도 부르지 않고 취소를 거절한다`() {
        place()
        participants.calls.clear()
        clock.advance(Duration.ofDays(7))

        assertFailsWith<OrderCancelPeriodExpiredException> { cancel() }

        assertEquals(emptyList(), participants.calls)
        assertEquals(OrderSagaStatus.COMPLETED, sagas.sagas.getValue(order.id).status)
        assertEquals(OrderStatus.COMPLETED, orderStatus())
    }

    @Test
    fun `기간이 지나도 끝나지 않은 주문은 중단할 수 있다`() {
        participants.pay = { "GATEWAY_DECLINED" }
        participants.restore = { throw ParticipantCallException("응답 없음") }
        place()
        participants.restore = { }
        clock.advance(Duration.ofDays(8))

        assertEquals(OrderSagaStatus.CANCELLED, cancel().status)
    }
}
