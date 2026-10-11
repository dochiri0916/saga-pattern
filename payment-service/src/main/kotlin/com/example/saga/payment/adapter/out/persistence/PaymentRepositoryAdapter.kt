package com.example.saga.payment.adapter.out.persistence

import com.example.saga.payment.adapter.out.persistence.mapper.toDomain
import com.example.saga.payment.adapter.out.persistence.mapper.toEntity
import com.example.saga.payment.adapter.out.persistence.repository.PaymentJpaRepository
import com.example.saga.payment.application.exception.PaymentAlreadyExistsException
import com.example.saga.payment.application.exception.PaymentConcurrentUpdateException
import com.example.saga.payment.application.port.out.PaymentRepository
import com.example.saga.payment.domain.payment.Payment
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentId
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryAdapter(
    private val paymentJpaRepository: PaymentJpaRepository,
) : PaymentRepository {

    override fun save(payment: Payment): Payment {
        val entity = paymentJpaRepository.findByPublicId(payment.id.value)
            ?.apply { updateStatus(payment.status.name) }
            ?: payment.toEntity()

        try {
            paymentJpaRepository.saveAndFlush(entity)
        } catch (e: OptimisticLockingFailureException) {
            throw PaymentConcurrentUpdateException(payment.id.toString(), e)
        } catch (e: DataIntegrityViolationException) {
            throw PaymentAlreadyExistsException(payment.orderId.toString(), e)
        }
        return payment
    }

    override fun findById(id: PaymentId): Payment? =
        paymentJpaRepository.findByPublicId(id.value)?.toDomain()

    override fun findByIdForUpdate(id: PaymentId): Payment? =
        paymentJpaRepository.findForUpdateByPublicId(id.value)?.toDomain()

    override fun findByOrderIdForUpdate(orderId: OrderReferenceId): Payment? =
        paymentJpaRepository.findForUpdateByOrderId(orderId.value)?.toDomain()

    override fun findByOrderId(orderId: OrderReferenceId): Payment? =
        paymentJpaRepository.findByOrderId(orderId.value)?.toDomain()

}
