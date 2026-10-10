package com.example.saga.order.application.saga

import com.example.saga.order.domain.vo.OrderId
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OrderSagaTest {

    private val startedAt = Instant.parse("2026-10-10T00:00:00Z")
    private val now = startedAt.plusSeconds(1)
    private val orderId = OrderId.of(UuidCreator.getTimeOrderedEpoch())

    private fun sagaAt(status: OrderSagaStatus, reason: String? = null): OrderSaga =
        OrderSaga.restore(orderId, status, reason, startedAt, startedAt)

    @Test
    fun `재고 차감과 결제가 되면 완료된다`() {
        val started = OrderSaga.start(orderId, startedAt)
        assertEquals(OrderSagaStatus.STOCK_REQUESTED, started.status)

        val paying = started.on(OrderSagaReply.STOCK_DECREASED, null, now)
        assertEquals(OrderSagaStatus.PAYMENT_REQUESTED, paying?.status)

        val completed = paying?.on(OrderSagaReply.PAYMENT_APPROVED, null, now)
        assertEquals(OrderSagaStatus.COMPLETED, completed?.status)
        assertNull(completed?.failureReason)
        assertEquals(now, completed?.updatedAt)
        assertEquals(startedAt, completed?.startedAt)
    }

    @Test
    fun `재고 차감이 거절되면 되돌릴 것 없이 바로 취소된다`() {
        val next = sagaAt(OrderSagaStatus.STOCK_REQUESTED).on(OrderSagaReply.STOCK_DECREASE_REJECTED, "INSUFFICIENT_STOCK", now)

        assertEquals(OrderSagaStatus.CANCELLED, next?.status)
        assertEquals("INSUFFICIENT_STOCK", next?.failureReason)
    }

    @Test
    fun `결제가 거절되면 재고를 되돌린 뒤 취소되고 사유가 유지된다`() {
        val restoring = sagaAt(OrderSagaStatus.PAYMENT_REQUESTED).on(OrderSagaReply.PAYMENT_REJECTED, "GATEWAY_DECLINED", now)
        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, restoring?.status)

        val cancelled = restoring?.on(OrderSagaReply.STOCK_RESTORED, null, now)
        assertEquals(OrderSagaStatus.CANCELLED, cancelled?.status)
        assertEquals("GATEWAY_DECLINED", cancelled?.failureReason)
    }

    @Test
    fun `지금 상태에서 기다리던 결과가 아니면 반영하지 않는다`() {
        assertNull(sagaAt(OrderSagaStatus.STOCK_REQUESTED).on(OrderSagaReply.PAYMENT_APPROVED, null, now))
        assertNull(sagaAt(OrderSagaStatus.STOCK_RESTORE_REQUESTED).on(OrderSagaReply.PAYMENT_APPROVED, null, now))
        assertNull(sagaAt(OrderSagaStatus.COMPLETED).on(OrderSagaReply.STOCK_DECREASED, null, now))
        assertNull(sagaAt(OrderSagaStatus.CANCELLED).on(OrderSagaReply.STOCK_RESTORED, null, now))
    }

    @Test
    fun `진행 중에 중단하면 하던 호출부터 되돌린다`() {
        val fromStock = sagaAt(OrderSagaStatus.STOCK_REQUESTED).abort(OrderSagaAbortReason.CALL_FAILED, now)
        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, fromStock?.status)
        assertEquals("CALL_FAILED", fromStock?.failureReason)

        val fromPayment = sagaAt(OrderSagaStatus.PAYMENT_REQUESTED).abort(OrderSagaAbortReason.TIMED_OUT, now)
        assertEquals(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, fromPayment?.status)
        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, fromPayment?.on(OrderSagaReply.PAYMENT_CANCELLED, null, now)?.status)
    }

    @Test
    fun `보상 중이거나 끝난 사가는 중단할 수 없다`() {
        val notAbortable = OrderSagaStatus.entries - OrderSagaStatus.ABORTABLE.toSet()

        notAbortable.forEach { assertNull(sagaAt(it).abort(OrderSagaAbortReason.TIMED_OUT, now), "status=$it") }
    }

    @Test
    fun `완료된 주문의 취소는 배송 취소부터 시작해 결제 취소와 재고 복원을 거친다`() {
        val cancelling = sagaAt(OrderSagaStatus.COMPLETED).cancel(now)
        assertEquals(OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED, cancelling?.status)

        val refunding = cancelling?.on(OrderSagaReply.SHIPMENT_CANCELLED, null, now)
        assertEquals(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, refunding?.status)

        val cancelled = refunding?.on(OrderSagaReply.PAYMENT_CANCELLED, null, now)?.on(OrderSagaReply.STOCK_RESTORED, null, now)
        assertEquals(OrderSagaStatus.CANCELLED, cancelled?.status)
        assertEquals("CANCEL_REQUESTED", cancelled?.failureReason)
    }

    @Test
    fun `배송 취소가 거절되면 완료 상태로 돌아가고 거절 사유가 남는다`() {
        val back = sagaAt(OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED, "CANCEL_REQUESTED")
            .on(OrderSagaReply.SHIPMENT_CANCEL_REJECTED, "ALREADY_SHIPPED", now)

        assertEquals(OrderSagaStatus.COMPLETED, back?.status)
        assertEquals("ALREADY_SHIPPED", back?.failureReason)
    }

    @Test
    fun `취소 요청은 진행 중이면 중단과 같고 이미 되돌리는 중이거나 취소됐으면 아무것도 하지 않는다`() {
        assertEquals(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, sagaAt(OrderSagaStatus.PAYMENT_REQUESTED).cancel(now)?.status)
        assertNull(sagaAt(OrderSagaStatus.STOCK_RESTORE_REQUESTED).cancel(now))
        assertNull(sagaAt(OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED).cancel(now))
        assertNull(sagaAt(OrderSagaStatus.CANCELLED).cancel(now))
    }

    @Test
    fun `다시 시도 표시는 상태와 사유를 그대로 두고 바뀐 시각만 고친다`() {
        val retrying = sagaAt(OrderSagaStatus.STOCK_RESTORE_REQUESTED, "CALL_FAILED").retrying(now)

        assertEquals(OrderSagaStatus.STOCK_RESTORE_REQUESTED, retrying.status)
        assertEquals("CALL_FAILED", retrying.failureReason)
        assertEquals(now, retrying.updatedAt)
    }
}
