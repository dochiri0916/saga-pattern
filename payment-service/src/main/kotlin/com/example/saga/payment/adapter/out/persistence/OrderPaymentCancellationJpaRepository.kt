package com.example.saga.payment.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface OrderPaymentCancellationJpaRepository : JpaRepository<OrderPaymentCancellationEntity, UUID> {

    @Modifying
    @Query(
        value = "insert ignore into order_payment_cancellations (order_id, cancelled_at) values (:orderId, :cancelledAt)",
        nativeQuery = true,
    )
    fun insertIgnore(orderId: UUID, cancelledAt: Instant): Int

    @Transactional
    @Modifying
    @Query(value = "delete from order_payment_cancellations where cancelled_at < :cutoff limit :batchSize", nativeQuery = true)
    fun deleteCancelledBefore(cutoff: Instant, batchSize: Int): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByOrderId(orderId: UUID): OrderPaymentCancellationEntity?
}
