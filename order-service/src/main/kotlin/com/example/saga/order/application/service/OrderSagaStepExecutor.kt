package com.example.saga.order.application.service

import com.example.saga.order.application.exception.OrderCancelRejectedException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderSagaNotFoundException
import com.example.saga.order.application.port.out.OrderEventPublisher
import com.example.saga.order.application.port.out.OrderIdempotencyKey
import com.example.saga.order.application.port.out.OrderIdempotencyKeyRepository
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.saga.OrderCancelPolicy
import com.example.saga.order.application.saga.OrderSaga
import com.example.saga.order.application.saga.OrderSagaAbortReason
import com.example.saga.order.application.saga.OrderSagaReply
import com.example.saga.order.application.saga.OrderSagaStatus
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.vo.OrderId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

private val log = LoggerFactory.getLogger(OrderSagaStepExecutor::class.java)

@Service
class OrderSagaStepExecutor(
    private val orderSagaRepository: OrderSagaRepository,
    private val orderRepository: OrderRepository,
    private val orderIdempotencyKeyRepository: OrderIdempotencyKeyRepository,
    private val orderEventPublisher: OrderEventPublisher,
    private val orderCancelPolicy: OrderCancelPolicy,
    private val clock: Clock,
) {

    @Transactional
    fun start(order: Order): OrderSaga {
        orderRepository.save(order)
        return orderSagaRepository.save(OrderSaga.start(order.id, clock.instant()))
    }

    @Transactional
    fun startOnce(order: Order, idempotencyKey: OrderIdempotencyKey): OrderSaga? {

        if (!orderIdempotencyKeyRepository.saveIfAbsent(idempotencyKey)) return null
        return start(order)
    }

    @Transactional
    fun apply(orderId: OrderId, expected: OrderSagaStatus, reply: OrderSagaReply, reason: String?): OrderSaga? {
        val saga = lock(orderId)
        val next = saga.takeIf { it.status == expected }?.on(reply, reason, clock.instant())
        if (next == null) {
            log.info("그 사이 사가가 옮겨져서 결과를 반영하지 않습니다. orderId={}, status={}, reply={}", orderId, saga.status, reply)
            return null
        }

        orderSagaRepository.save(next)
        when {

            reply == OrderSagaReply.PAYMENT_APPROVED -> {
                val completed = orderRepository.save(find(orderId).complete())
                orderEventPublisher.publish(completed)
            }
            next.status == OrderSagaStatus.CANCELLED -> orderEventPublisher.publish(orderRepository.save(find(orderId).cancel()))
        }
        log.info("사가를 다음 단계로 옮겼습니다. orderId={}, {} -> {}, reason={}", orderId, saga.status, next.status, next.failureReason)
        return next
    }

    @Transactional
    fun abort(orderId: OrderId, reason: OrderSagaAbortReason): OrderSaga {
        val saga = lock(orderId)
        val next = saga.abort(reason, clock.instant()) ?: return saga
        log.warn("사가를 중단했습니다. orderId={}, {} -> {}, reason={}", orderId, saga.status, next.status, reason)
        return orderSagaRepository.save(next)
    }

    @Transactional
    fun cancel(orderId: OrderId): OrderSaga {
        val saga = lock(orderId)
        val now = clock.instant()

        if (saga.status == OrderSagaStatus.COMPLETED && orderCancelPolicy.expired(saga.startedAt, now)) {
            throw OrderCancelRejectedException(orderId.toString(), OrderCancelPolicy.PERIOD_EXPIRED)
        }
        val next = saga.cancel(now) ?: return saga
        log.info("취소 요청으로 사가를 옮겼습니다. orderId={}, {} -> {}", orderId, saga.status, next.status)
        return orderSagaRepository.save(next)
    }

    @Transactional
    fun claimStalled(before: Instant, limit: Int): List<OrderSaga> {
        val now = clock.instant()
        return orderSagaRepository.findStalledForUpdate(before, limit).map { saga ->
            orderSagaRepository.save(saga.abort(OrderSagaAbortReason.TIMED_OUT, now) ?: saga.retrying(now))
        }
    }

    private fun lock(orderId: OrderId): OrderSaga =
        orderSagaRepository.findByOrderIdForUpdate(orderId) ?: throw OrderSagaNotFoundException(orderId.toString())

    private fun find(orderId: OrderId): Order =
        orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId.toString())
}
