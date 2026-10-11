package com.example.saga.order.adapter.out.persistence.repository

import com.example.saga.order.adapter.out.persistence.entity.OrderSagaEntity
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints

interface OrderSagaJpaRepository : JpaRepository<OrderSagaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByOrderId(orderId: UUID): OrderSagaEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    fun findByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
        statuses: Collection<String>,
        updatedAt: Instant,
        limit: Limit,
    ): List<OrderSagaEntity>

    fun countByStatusInAndStartedAtBefore(statuses: Collection<String>, startedAt: Instant): Long
}
