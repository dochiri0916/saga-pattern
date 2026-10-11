package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import java.time.Instant

interface OrderPaymentCancellationRepository {

    fun saveIfAbsent(orderId: OrderReferenceId)

    fun existsForUpdate(orderId: OrderReferenceId): Boolean

    fun deleteCancelledBefore(before: Instant, limit: Int): Int
}
