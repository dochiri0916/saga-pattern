package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.Payment
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentId

interface PaymentRepository {

    fun save(payment: Payment): Payment

    fun findById(id: PaymentId): Payment?

    fun findByIdForUpdate(id: PaymentId): Payment?

    fun findByOrderId(orderId: OrderId): Payment?

    fun findByOrderIdForUpdate(orderId: OrderId): Payment?
}
