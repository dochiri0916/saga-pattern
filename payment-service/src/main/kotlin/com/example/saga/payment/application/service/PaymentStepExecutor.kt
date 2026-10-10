package com.example.saga.payment.application.service

import com.example.saga.payment.application.exception.PaymentAlreadyExistsException
import com.example.saga.payment.application.exception.PaymentNotFoundException
import com.example.saga.payment.application.exception.PaymentRejectedException
import com.example.saga.payment.application.exception.PaymentRejectionReason
import com.example.saga.payment.application.port.out.OrderPaymentCancellationRepository
import com.example.saga.payment.application.port.out.PaymentIdGenerator
import com.example.saga.payment.application.port.out.PaymentRepository
import com.example.saga.payment.domain.Payment
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import com.example.saga.payment.domain.vo.PaymentId
import com.example.saga.payment.domain.vo.PaymentStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentStepExecutor(
    private val orderPaymentCancellationRepository: OrderPaymentCancellationRepository,
    private val paymentIdGenerator: PaymentIdGenerator,
    private val paymentRepository: PaymentRepository,
) {

    @Transactional
    fun register(orderId: OrderId, amount: PaymentAmount): Payment {

        if (orderPaymentCancellationRepository.existsForUpdate(orderId)) {
            throw PaymentRejectedException(orderId.toString(), PaymentRejectionReason.ORDER_CANCELLED)
        }

        val existing = paymentRepository.findByOrderId(orderId)
            ?: return paymentRepository.save(Payment.register(paymentIdGenerator.generate(), orderId, amount))
        return when (existing.status) {
            PaymentStatus.COMPLETED -> existing
            PaymentStatus.PENDING -> throw PaymentAlreadyExistsException(orderId.toString())
            PaymentStatus.REJECTED -> throw PaymentRejectedException(orderId.toString(), PaymentRejectionReason.GATEWAY_DECLINED)
            PaymentStatus.CANCELLED -> throw PaymentRejectedException(orderId.toString(), PaymentRejectionReason.PAYMENT_CANCELLED)
        }
    }

    @Transactional
    fun settle(paymentId: PaymentId, approved: Boolean): Payment {
        val payment = paymentRepository.findByIdForUpdate(paymentId) ?: throw PaymentNotFoundException(paymentId.toString())
        if (payment.status != PaymentStatus.PENDING) return payment
        return paymentRepository.save(if (approved) payment.approve() else payment.reject())
    }

    @Transactional
    fun cancel(orderId: OrderId): Payment? {

        orderPaymentCancellationRepository.saveIfAbsent(orderId)

        val payment = paymentRepository.findByOrderIdForUpdate(orderId) ?: return null
        return when (payment.status) {
            PaymentStatus.PENDING, PaymentStatus.COMPLETED -> paymentRepository.save(payment.cancel())

            PaymentStatus.CANCELLED -> payment
            PaymentStatus.REJECTED -> null
        }
    }
}
