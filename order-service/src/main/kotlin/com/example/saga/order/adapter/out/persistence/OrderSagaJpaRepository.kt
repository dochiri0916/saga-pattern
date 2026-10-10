package com.example.saga.order.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints
import jakarta.persistence.QueryHint
import java.time.Instant
import java.util.UUID

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
