package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.payment.Payment
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentId

interface PaymentRepository {

    fun save(payment: Payment): Payment

    fun findById(id: PaymentId): Payment?

    fun findByIdForUpdate(id: PaymentId): Payment?

    fun findByOrderId(orderId: OrderReferenceId): Payment?

    fun findByOrderIdForUpdate(orderId: OrderReferenceId): Payment?
}
