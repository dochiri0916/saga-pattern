package com.example.saga.order.application.saga

import com.example.saga.order.domain.vo.OrderId
import java.time.Instant

class OrderSaga private constructor(
    val orderId: OrderId,
    val status: OrderSagaStatus,

    val failureReason: String?,
    val startedAt: Instant,

    val updatedAt: Instant,
) {

    fun on(reply: OrderSagaReply, reason: String?, now: Instant): OrderSaga? = when (status to reply) {
        OrderSagaStatus.STOCK_REQUESTED to OrderSagaReply.STOCK_DECREASED -> moveTo(OrderSagaStatus.PAYMENT_REQUESTED, now)
        OrderSagaStatus.STOCK_REQUESTED to OrderSagaReply.STOCK_DECREASE_REJECTED -> moveTo(OrderSagaStatus.CANCELLED, now, reason)
        OrderSagaStatus.PAYMENT_REQUESTED to OrderSagaReply.PAYMENT_APPROVED -> moveTo(OrderSagaStatus.COMPLETED, now)
        OrderSagaStatus.PAYMENT_REQUESTED to OrderSagaReply.PAYMENT_REJECTED ->
            moveTo(OrderSagaStatus.STOCK_RESTORE_REQUESTED, now, reason)
        OrderSagaStatus.PAYMENT_CANCEL_REQUESTED to OrderSagaReply.PAYMENT_CANCELLED ->
            moveTo(OrderSagaStatus.STOCK_RESTORE_REQUESTED, now)
        OrderSagaStatus.STOCK_RESTORE_REQUESTED to OrderSagaReply.STOCK_RESTORED -> moveTo(OrderSagaStatus.CANCELLED, now)
        OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED to OrderSagaReply.SHIPMENT_CANCELLED ->
            moveTo(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, now)
        OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED to OrderSagaReply.SHIPMENT_CANCEL_REJECTED -> moveTo(OrderSagaStatus.COMPLETED, now, reason)
        else -> null
    }

    fun abort(reason: OrderSagaAbortReason, now: Instant): OrderSaga? = when (status) {
        OrderSagaStatus.STOCK_REQUESTED -> moveTo(OrderSagaStatus.STOCK_RESTORE_REQUESTED, now, reason.name)
        OrderSagaStatus.PAYMENT_REQUESTED -> moveTo(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, now, reason.name)
        else -> null
    }

    fun cancel(now: Instant): OrderSaga? = when (status) {
        OrderSagaStatus.COMPLETED -> moveTo(OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED, now, OrderSagaAbortReason.CANCEL_REQUESTED.name)
        else -> abort(OrderSagaAbortReason.CANCEL_REQUESTED, now)
    }

    fun retrying(now: Instant): OrderSaga = moveTo(status, now)

    private fun moveTo(next: OrderSagaStatus, now: Instant, reason: String? = failureReason): OrderSaga =
        OrderSaga(orderId, next, reason, startedAt, now)

    override fun toString(): String = "OrderSaga(orderId=$orderId, status=$status, failureReason=$failureReason)"

    companion object {

        fun start(orderId: OrderId, now: Instant): OrderSaga =
            OrderSaga(orderId, OrderSagaStatus.STOCK_REQUESTED, null, now, now)

        fun restore(
            orderId: OrderId,
            status: OrderSagaStatus,
            failureReason: String?,
            startedAt: Instant,
            updatedAt: Instant,
        ): OrderSaga = OrderSaga(orderId, status, failureReason, startedAt, updatedAt)
    }
}

enum class OrderSagaStatus(

    val waiting: Boolean,
) {
    STOCK_REQUESTED(true),
    PAYMENT_REQUESTED(true),

    PAYMENT_CANCEL_REQUESTED(true),

    STOCK_RESTORE_REQUESTED(true),

    SHIPMENT_CANCEL_REQUESTED(true),
    COMPLETED(false),
    CANCELLED(false);

    companion object {
        val WAITING: List<OrderSagaStatus> = entries.filter { it.waiting }

        val ABORTABLE: List<OrderSagaStatus> = listOf(STOCK_REQUESTED, PAYMENT_REQUESTED)
    }
}

enum class OrderSagaReply {
    STOCK_DECREASED,
    STOCK_DECREASE_REJECTED,
    PAYMENT_APPROVED,
    PAYMENT_REJECTED,
    STOCK_RESTORED,
    PAYMENT_CANCELLED,
    SHIPMENT_CANCELLED,
    SHIPMENT_CANCEL_REJECTED,
}

enum class OrderSagaAbortReason {

    CALL_FAILED,

    TIMED_OUT,
    CANCEL_REQUESTED,
}
