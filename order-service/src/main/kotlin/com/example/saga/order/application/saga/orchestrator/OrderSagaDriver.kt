package com.example.saga.order.application.saga.orchestrator

import com.example.saga.order.application.port.out.InventoryClient
import com.example.saga.order.application.port.out.ParticipantCallException
import com.example.saga.order.application.port.out.ParticipantUnavailableException
import com.example.saga.order.application.port.out.PaymentClient
import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.application.saga.model.OrderSaga
import com.example.saga.order.application.saga.model.OrderSagaAbortReason
import com.example.saga.order.application.saga.model.OrderSagaReply
import com.example.saga.order.application.saga.model.OrderSagaStatus
import com.example.saga.order.domain.order.Order
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(OrderSagaDriver::class.java)

@Service
class OrderSagaDriver(
    private val orderSagaStepExecutor: OrderSagaStepExecutor,
    private val inventoryClient: InventoryClient,
    private val paymentClient: PaymentClient,
    private val shippingClient: ShippingClient,
) {

    fun drive(order: Order, from: OrderSaga): OrderSaga {
        var saga = from
        while (saga.status.waiting) {
            val next = step(order, saga) ?: break
            if (next.status == saga.status) break
            saga = next
        }
        return saga
    }

    private fun step(order: Order, saga: OrderSaga): OrderSaga? {
        val outcome = try {
            call(order, saga.status)
        } catch (e: ParticipantUnavailableException) {
            log.warn("참여 서비스에 요청을 보내지 못했습니다. orderId={}, status={}, cause={}", order.id, saga.status, e.message)

            return when (saga.status) {
                OrderSagaStatus.STOCK_REQUESTED ->
                    orderSagaStepExecutor.apply(order.id, saga.status, OrderSagaReply.STOCK_DECREASE_REJECTED, INVENTORY_UNAVAILABLE)
                OrderSagaStatus.PAYMENT_REQUESTED ->
                    orderSagaStepExecutor.apply(order.id, saga.status, OrderSagaReply.PAYMENT_REJECTED, PAYMENT_UNAVAILABLE)
                else -> null
            }
        } catch (e: ParticipantCallException) {
            log.warn("참여 서비스 호출의 결과를 알 수 없습니다. orderId={}, status={}", order.id, saga.status, e)

            return if (saga.status in OrderSagaStatus.ABORTABLE) {
                orderSagaStepExecutor.abort(order.id, OrderSagaAbortReason.CALL_FAILED)
            } else {
                null
            }
        }
        return orderSagaStepExecutor.apply(order.id, saga.status, outcome.reply, outcome.reason)
    }

    private fun call(order: Order, status: OrderSagaStatus): Outcome = when (status) {
        OrderSagaStatus.STOCK_REQUESTED ->
            inventoryClient.decreaseStock(order)?.let { Outcome(OrderSagaReply.STOCK_DECREASE_REJECTED, it) }
                ?: Outcome(OrderSagaReply.STOCK_DECREASED)
        OrderSagaStatus.PAYMENT_REQUESTED ->
            paymentClient.pay(order)?.let { Outcome(OrderSagaReply.PAYMENT_REJECTED, it) }
                ?: Outcome(OrderSagaReply.PAYMENT_APPROVED)
        OrderSagaStatus.PAYMENT_CANCEL_REQUESTED -> {
            paymentClient.cancelPayment(order)
            Outcome(OrderSagaReply.PAYMENT_CANCELLED)
        }
        OrderSagaStatus.STOCK_RESTORE_REQUESTED -> {
            inventoryClient.restoreStock(order)
            Outcome(OrderSagaReply.STOCK_RESTORED)
        }
        OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED ->
            shippingClient.cancelShipment(order)?.let { Outcome(OrderSagaReply.SHIPMENT_CANCEL_REJECTED, it) }
                ?: Outcome(OrderSagaReply.SHIPMENT_CANCELLED)
        OrderSagaStatus.COMPLETED, OrderSagaStatus.CANCELLED -> error("끝난 사가는 진행시킬 수 없습니다. orderId=${order.id}")
    }

    private data class Outcome(val reply: OrderSagaReply, val reason: String? = null)

    private companion object {
        const val INVENTORY_UNAVAILABLE = "INVENTORY_UNAVAILABLE"
        const val PAYMENT_UNAVAILABLE = "PAYMENT_UNAVAILABLE"
    }
}
