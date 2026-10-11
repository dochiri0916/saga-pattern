package com.example.saga.order.adapter.out.persistence.mapper

import com.example.saga.order.adapter.out.persistence.entity.OrderSagaEntity
import com.example.saga.order.application.saga.model.OrderSaga
import com.example.saga.order.application.saga.model.OrderSagaStatus
import com.example.saga.order.domain.order.vo.OrderId

fun OrderSagaEntity.toSaga(): OrderSaga = OrderSaga.restore(
    orderId = OrderId.of(orderId),
    status = OrderSagaStatus.valueOf(status),
    failureReason = failureReason,
    startedAt = startedAt,
    updatedAt = updatedAt,
)
