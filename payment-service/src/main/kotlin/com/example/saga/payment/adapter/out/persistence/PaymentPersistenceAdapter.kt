package com.example.saga.payment.adapter.out.persistence

import com.example.saga.payment.application.exception.PaymentAlreadyExistsException
import com.example.saga.payment.application.exception.PaymentConcurrentUpdateException
import com.example.saga.payment.application.port.out.PaymentRepository
import com.example.saga.payment.domain.Payment
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import com.example.saga.payment.domain.vo.PaymentId
import com.example.saga.payment.domain.vo.PaymentStatus
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
class PaymentPersistenceAdapter(
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

    override fun findByOrderIdForUpdate(orderId: OrderId): Payment? =
        paymentJpaRepository.findForUpdateByOrderId(orderId.value)?.toDomain()

    override fun findByOrderId(orderId: OrderId): Payment? =
        paymentJpaRepository.findByOrderId(orderId.value)?.toDomain()

    private fun Payment.toEntity(): PaymentEntity = PaymentEntity.of(
        publicId = id.value,
        orderId = orderId.value,
        amount = amount.value,
        status = status.name,
    )

    private fun PaymentEntity.toDomain(): Payment = Payment.restore(
        id = PaymentId.of(publicId),
        orderId = OrderId.of(orderId),
        amount = PaymentAmount.of(amount),
        status = PaymentStatus.valueOf(status),
    )
}
