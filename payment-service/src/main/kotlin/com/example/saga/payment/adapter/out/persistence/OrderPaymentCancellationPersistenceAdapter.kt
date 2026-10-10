package com.example.saga.payment.adapter.out.persistence

import com.example.saga.payment.application.port.out.OrderPaymentCancellationRepository
import com.example.saga.payment.domain.vo.OrderId
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.Instant

@Repository
class OrderPaymentCancellationPersistenceAdapter(
    private val orderPaymentCancellationJpaRepository: OrderPaymentCancellationJpaRepository,
    private val clock: Clock,
) : OrderPaymentCancellationRepository {

    override fun saveIfAbsent(orderId: OrderId) {
        orderPaymentCancellationJpaRepository.insertIgnore(orderId.value, clock.instant())
    }

    override fun deleteCancelledBefore(before: Instant, limit: Int): Int =
        orderPaymentCancellationJpaRepository.deleteCancelledBefore(before, limit)

    override fun existsForUpdate(orderId: OrderId): Boolean =
        orderPaymentCancellationJpaRepository.findForUpdateByOrderId(orderId.value) != null
}
