package com.example.saga.payment.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.UUID

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {

    fun findByPublicId(publicId: UUID): PaymentEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByPublicId(publicId: UUID): PaymentEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByOrderId(orderId: UUID): PaymentEntity?

    fun findByOrderId(orderId: UUID): PaymentEntity?
}
