package com.example.saga.order.application.port.out

import com.example.saga.order.domain.vo.OrderId
import java.time.Instant

interface OrderIdempotencyKeyRepository {

    fun saveIfAbsent(idempotencyKey: OrderIdempotencyKey): Boolean

    fun findByKey(key: String): OrderIdempotencyKey?

    fun deleteCreatedBefore(before: Instant, limit: Int): Int
}

data class OrderIdempotencyKey(
    val key: String,
    val orderId: OrderId,

    val requestHash: String,
)
