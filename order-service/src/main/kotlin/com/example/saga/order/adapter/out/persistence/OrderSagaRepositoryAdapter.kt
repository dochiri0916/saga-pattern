package com.example.saga.order.adapter.out.persistence

import com.example.saga.order.adapter.out.persistence.entity.OrderSagaEntity
import com.example.saga.order.adapter.out.persistence.mapper.toSaga
import com.example.saga.order.adapter.out.persistence.repository.OrderSagaJpaRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.saga.model.OrderSaga
import com.example.saga.order.application.saga.model.OrderSagaStatus
import com.example.saga.order.domain.order.vo.OrderId
import java.time.Instant
import org.springframework.data.domain.Limit
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class OrderSagaRepositoryAdapter(
    private val orderSagaJpaRepository: OrderSagaJpaRepository,
) : OrderSagaRepository {

    override fun save(saga: OrderSaga): OrderSaga {
        val entity = orderSagaJpaRepository.findByIdOrNull(saga.orderId.value)
            ?.apply { update(saga.status.name, saga.failureReason, saga.updatedAt) }
            ?: OrderSagaEntity.of(saga.orderId.value, saga.status.name, saga.failureReason, saga.startedAt, saga.updatedAt)

        orderSagaJpaRepository.saveAndFlush(entity)
        return saga
    }

    override fun findByOrderId(orderId: OrderId): OrderSaga? =
        orderSagaJpaRepository.findByIdOrNull(orderId.value)?.toSaga()

    override fun findByOrderIdForUpdate(orderId: OrderId): OrderSaga? =
        orderSagaJpaRepository.findForUpdateByOrderId(orderId.value)?.toSaga()

    override fun findStalledForUpdate(before: Instant, limit: Int): List<OrderSaga> =
        orderSagaJpaRepository
            .findByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(OrderSagaStatus.WAITING.map { it.name }, before, Limit.of(limit))
            .map { it.toSaga() }

    override fun countUnfinishedStartedBefore(before: Instant): Long =
        orderSagaJpaRepository.countByStatusInAndStartedAtBefore(OrderSagaStatus.WAITING.map { it.name }, before)

}
