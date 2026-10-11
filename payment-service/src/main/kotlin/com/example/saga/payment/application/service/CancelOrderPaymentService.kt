package com.example.saga.payment.application.service

import com.example.saga.payment.application.command.CancelOrderPaymentCommand
import com.example.saga.payment.application.port.`in`.CancelOrderPaymentUseCase
import com.example.saga.payment.application.port.out.PaymentGateway
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(CancelOrderPaymentService::class.java)

@Service
class CancelOrderPaymentService(
    private val paymentStepExecutor: PaymentStepExecutor,
    private val paymentGateway: PaymentGateway,
) : CancelOrderPaymentUseCase {

    override fun cancel(command: CancelOrderPaymentCommand) {
        val orderId = OrderReferenceId.of(command.orderId)
        val payment = paymentStepExecutor.cancel(orderId)
        if (payment == null) {
            log.info("취소할 결제가 없습니다. orderId={}", orderId)
            return
        }
        paymentGateway.cancel(payment)
        log.info("주문의 결제를 취소했습니다. orderId={}, paymentId={}", orderId, payment.id)
    }
}
