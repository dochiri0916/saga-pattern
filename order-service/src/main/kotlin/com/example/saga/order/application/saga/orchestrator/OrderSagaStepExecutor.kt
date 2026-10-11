package com.example.saga.order.application.saga.orchestrator

import com.example.saga.order.application.exception.OrderCancelPeriodExpiredException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderSagaNotFoundException
import com.example.saga.order.application.integration.mapper.OrderIntegrationEventMapper
import com.example.saga.order.application.port.out.AuditTrail
import com.example.saga.order.application.port.out.EventPublicationPort
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.saga.model.OrderSaga
import com.example.saga.order.application.saga.model.OrderSagaAbortReason
import com.example.saga.order.application.saga.model.OrderSagaReply
import com.example.saga.order.application.saga.model.OrderSagaStatus
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.OrderChange
import com.example.saga.order.domain.order.policy.OrderCancelPolicy
import com.example.saga.order.domain.order.vo.OrderId
import java.time.Clock
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(OrderSagaStepExecutor::class.java)

@Service
class OrderSagaStepExecutor(
    private val orderSagaRepository: OrderSagaRepository,
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: EventPublicationPort,
    private val orderCancelPolicy: OrderCancelPolicy,
    private val clock: Clock,
    private val auditTrail: AuditTrail,
) {

    @Transactional
    fun start(order: Order): OrderSaga {
        orderRepository.save(order)
        return orderSagaRepository.save(OrderSaga.start(order.id, clock.instant())).also { auditTrail.record(order.id.toString(), "ORDER_STARTED", it.status.name) }
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
        auditTrail.record(orderId.toString(), reply.name, "${saga.status} -> ${next.status}; reason=${next.failureReason}")
        when {

            reply == OrderSagaReply.PAYMENT_APPROVED -> record(find(orderId).complete())
            next.status == OrderSagaStatus.CANCELLED -> record(find(orderId).cancel())
        }
        log.info("사가를 다음 단계로 옮겼습니다. orderId={}, {} -> {}, reason={}", orderId, saga.status, next.status, next.failureReason)
        return next
    }

    @Transactional
    fun abort(orderId: OrderId, reason: OrderSagaAbortReason): OrderSaga {
        val saga = lock(orderId)
        val next = saga.abort(reason, clock.instant()) ?: return saga
        log.warn("사가를 중단했습니다. orderId={}, {} -> {}, reason={}", orderId, saga.status, next.status, reason)
        return orderSagaRepository.save(next).also { auditTrail.record(orderId.toString(), "SAGA_${next.status}", next.failureReason) }
    }

    @Transactional
    fun cancel(orderId: OrderId): OrderSaga {
        val saga = lock(orderId)
        val now = clock.instant()

        if (saga.status == OrderSagaStatus.COMPLETED && orderCancelPolicy.expired(saga.startedAt, now)) {
            throw OrderCancelPeriodExpiredException(orderId.toString())
        }
        val next = saga.cancel(now) ?: return saga
        log.info("취소 요청으로 사가를 옮겼습니다. orderId={}, {} -> {}", orderId, saga.status, next.status)
        return orderSagaRepository.save(next).also { auditTrail.record(orderId.toString(), "SAGA_${next.status}", next.failureReason) }
    }

    @Transactional
    fun claimStalled(before: Instant, limit: Int): List<OrderSaga> {
        val now = clock.instant()
        return orderSagaRepository.findStalledForUpdate(before, limit).map { saga ->
            orderSagaRepository.save(saga.abort(OrderSagaAbortReason.TIMED_OUT, now) ?: saga.claimedForRecovery(now))
                .also { auditTrail.record(saga.orderId.toString(), "RECOVERY_CLAIMED", it.status.name) }
        }
    }

    private fun lock(orderId: OrderId): OrderSaga =
        orderSagaRepository.findByOrderIdForUpdate(orderId) ?: throw OrderSagaNotFoundException(orderId.toString())

    private fun find(orderId: OrderId): Order =
        orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId.toString())

    private fun record(change: OrderChange) {
        orderRepository.save(change.order)
        OrderIntegrationEventMapper.map(change).forEach(orderEventPublisher::publish)
    }
}
