package com.example.saga.payment.adapter.out.persistence.repository

import com.example.saga.payment.adapter.out.persistence.entity.PaymentEntity
import jakarta.persistence.LockModeType
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {

    fun findByPublicId(publicId: UUID): PaymentEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByPublicId(publicId: UUID): PaymentEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByOrderId(orderId: UUID): PaymentEntity?

    fun findByOrderId(orderId: UUID): PaymentEntity?
}
