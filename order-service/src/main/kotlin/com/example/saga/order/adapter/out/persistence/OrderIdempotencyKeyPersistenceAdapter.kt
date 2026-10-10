package com.example.saga.order.adapter.out.persistence

import com.example.saga.order.application.port.out.OrderIdempotencyKey
import com.example.saga.order.application.port.out.OrderIdempotencyKeyRepository
import com.example.saga.order.domain.vo.OrderId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.Instant

@Repository
class OrderIdempotencyKeyPersistenceAdapter(
    private val orderIdempotencyKeyJpaRepository: OrderIdempotencyKeyJpaRepository,
    private val clock: Clock,
) : OrderIdempotencyKeyRepository {

    override fun saveIfAbsent(idempotencyKey: OrderIdempotencyKey): Boolean = orderIdempotencyKeyJpaRepository.insertIgnore(
        idempotencyKey = idempotencyKey.key,
        orderId = idempotencyKey.orderId.value,
        requestHash = idempotencyKey.requestHash,
        createdAt = clock.instant(),
    ) == 1

    override fun findByKey(key: String): OrderIdempotencyKey? =
        orderIdempotencyKeyJpaRepository.findByIdOrNull(key)
            ?.let { OrderIdempotencyKey(it.idempotencyKey, OrderId.of(it.orderId), it.requestHash) }

    override fun deleteCreatedBefore(before: Instant, limit: Int): Int =
        orderIdempotencyKeyJpaRepository.deleteCreatedBefore(before, limit)
}
