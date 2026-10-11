package com.example.saga.order.application.saga.model

import com.example.saga.order.domain.order.policy.OrderCompensation
import com.example.saga.order.domain.order.policy.OrderCompensationPolicy
import com.example.saga.order.domain.order.policy.OrderFailure
import com.example.saga.order.domain.order.vo.OrderId
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
        OrderSagaStatus.STOCK_REQUESTED to OrderSagaReply.STOCK_DECREASE_REJECTED -> compensate(OrderFailure.STOCK_REJECTED, now, reason)
        OrderSagaStatus.PAYMENT_REQUESTED to OrderSagaReply.PAYMENT_APPROVED -> moveTo(OrderSagaStatus.COMPLETED, now)
        OrderSagaStatus.PAYMENT_REQUESTED to OrderSagaReply.PAYMENT_REJECTED ->
            compensate(OrderFailure.PAYMENT_REJECTED, now, reason)
        OrderSagaStatus.PAYMENT_CANCEL_REQUESTED to OrderSagaReply.PAYMENT_CANCELLED ->
            compensated(OrderCompensation.CANCEL_PAYMENT, now)
        OrderSagaStatus.STOCK_RESTORE_REQUESTED to OrderSagaReply.STOCK_RESTORED -> compensated(OrderCompensation.RESTORE_STOCK, now)
        OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED to OrderSagaReply.SHIPMENT_CANCELLED ->
            compensated(OrderCompensation.CANCEL_SHIPMENT, now)
        OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED to OrderSagaReply.SHIPMENT_CANCEL_REJECTED -> moveTo(OrderSagaStatus.COMPLETED, now, reason)
        else -> null
    }

    fun abort(reason: OrderSagaAbortReason, now: Instant): OrderSaga? = when (status) {
        OrderSagaStatus.STOCK_REQUESTED -> compensate(OrderFailure.STOCK_UNCERTAIN, now, reason.name)
        OrderSagaStatus.PAYMENT_REQUESTED -> compensate(OrderFailure.PAYMENT_UNCERTAIN, now, reason.name)
        else -> null
    }

    fun cancel(now: Instant): OrderSaga? = when (status) {
        OrderSagaStatus.COMPLETED -> compensate(OrderFailure.COMPLETED_CANCEL, now, OrderSagaAbortReason.CANCEL_REQUESTED.name)
        else -> abort(OrderSagaAbortReason.CANCEL_REQUESTED, now)
    }

    fun claimedForRecovery(now: Instant): OrderSaga = moveTo(status, now)

    private fun compensate(failure: OrderFailure, now: Instant, reason: String?): OrderSaga =
        moveTo(OrderCompensationPolicy.after(failure).toStatus(), now, reason)

    private fun compensated(step: OrderCompensation, now: Instant): OrderSaga =
        moveTo(OrderCompensationPolicy.afterCompensation(step).toStatus(), now)

    private fun OrderCompensation.toStatus(): OrderSagaStatus = when (this) {
        OrderCompensation.CANCEL_SHIPMENT -> OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED
        OrderCompensation.CANCEL_PAYMENT -> OrderSagaStatus.PAYMENT_CANCEL_REQUESTED
        OrderCompensation.RESTORE_STOCK -> OrderSagaStatus.STOCK_RESTORE_REQUESTED
        OrderCompensation.DONE -> OrderSagaStatus.CANCELLED
    }

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
