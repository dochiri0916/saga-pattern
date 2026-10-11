package com.example.saga.payment.application.service

import com.example.saga.payment.application.exception.PaymentNotFoundException
import com.example.saga.payment.application.port.`in`.GetPaymentUseCase
import com.example.saga.payment.application.port.out.PaymentRepository
import com.example.saga.payment.application.query.GetPaymentQuery
import com.example.saga.payment.application.result.GetPaymentResult
import com.example.saga.payment.domain.payment.vo.PaymentId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPaymentService(
    private val paymentRepository: PaymentRepository,
) : GetPaymentUseCase {

    @Transactional(readOnly = true)
    override fun get(query: GetPaymentQuery): GetPaymentResult {
        val payment = paymentRepository.findById(PaymentId.of(query.paymentId))
            ?: throw PaymentNotFoundException(query.paymentId.toString())

        return GetPaymentResult.from(payment)
    }
}
