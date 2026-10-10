package com.example.saga.order.application.port.out

import com.example.saga.order.application.saga.OrderSaga
import com.example.saga.order.domain.vo.OrderId
import java.time.Instant

interface OrderSagaRepository {
    fun save(saga: OrderSaga): OrderSaga

    fun findByOrderId(orderId: OrderId): OrderSaga?

    fun findByOrderIdForUpdate(orderId: OrderId): OrderSaga?

    fun findStalledForUpdate(before: Instant, limit: Int): List<OrderSaga>

    fun countUnfinishedStartedBefore(before: Instant): Long
}
