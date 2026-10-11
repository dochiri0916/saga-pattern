package com.example.saga.payment.adapter.out.persistence.mapper

import com.example.saga.payment.adapter.out.persistence.entity.PaymentEntity
import com.example.saga.payment.domain.payment.Payment
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentAmount
import com.example.saga.payment.domain.payment.vo.PaymentId
import com.example.saga.payment.domain.payment.vo.PaymentStatus

fun Payment.toEntity(): PaymentEntity = PaymentEntity.of(
    publicId = id.value,
    orderId = orderId.value,
    amount = amount.value,
    status = status.name,
)

fun PaymentEntity.toDomain(): Payment = Payment.restore(
    id = PaymentId.of(publicId),
    orderId = OrderReferenceId.of(orderId),
    amount = PaymentAmount.of(amount),
    status = PaymentStatus.valueOf(status),
)
