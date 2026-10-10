package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.vo.OrderId
import java.time.Instant

interface OrderPaymentCancellationRepository {

    fun saveIfAbsent(orderId: OrderId)

    fun existsForUpdate(orderId: OrderId): Boolean

    fun deleteCancelledBefore(before: Instant, limit: Int): Int
}
