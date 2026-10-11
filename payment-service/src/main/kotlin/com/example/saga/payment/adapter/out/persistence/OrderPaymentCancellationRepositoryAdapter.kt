package com.example.saga.payment.adapter.out.persistence

import com.example.saga.payment.adapter.out.persistence.repository.OrderPaymentCancellationJpaRepository
import com.example.saga.payment.application.port.out.OrderPaymentCancellationRepository
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import java.time.Clock
import java.time.Instant
import org.springframework.stereotype.Repository

@Repository
class OrderPaymentCancellationRepositoryAdapter(
    private val orderPaymentCancellationJpaRepository: OrderPaymentCancellationJpaRepository,
    private val clock: Clock,
) : OrderPaymentCancellationRepository {

    override fun saveIfAbsent(orderId: OrderReferenceId) {
        orderPaymentCancellationJpaRepository.insertIgnore(orderId.value, clock.instant())
    }

    override fun deleteCancelledBefore(before: Instant, limit: Int): Int =
        orderPaymentCancellationJpaRepository.deleteCancelledBefore(before, limit)

    override fun existsForUpdate(orderId: OrderReferenceId): Boolean =
        orderPaymentCancellationJpaRepository.findForUpdateByOrderId(orderId.value) != null
}
