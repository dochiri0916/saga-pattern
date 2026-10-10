package com.example.saga.payment.application.service

import com.example.saga.payment.application.exception.PaymentRejectedException
import com.example.saga.payment.application.exception.PaymentRejectionReason
import com.example.saga.payment.application.port.`in`.command.PayCommand
import com.example.saga.payment.application.port.`in`.result.PayResult
import com.example.saga.payment.application.port.`in`.usecase.PayUseCase
import com.example.saga.payment.application.port.out.PaymentGateway
import com.example.saga.payment.domain.exception.InvalidPaymentAmountException
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import com.example.saga.payment.domain.vo.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(PayService::class.java)

@Service
class PayService(
    private val paymentStepExecutor: PaymentStepExecutor,
    private val paymentGateway: PaymentGateway,
) : PayUseCase {

    override fun pay(command: PayCommand): PayResult {
        val orderId = OrderId.of(command.orderId)
        val amount = try {
            PaymentAmount.of(Math.toIntExact(command.amount))
        } catch (e: ArithmeticException) {
            throw rejected(orderId, PaymentRejectionReason.INVALID_AMOUNT)
        } catch (e: InvalidPaymentAmountException) {
            throw rejected(orderId, PaymentRejectionReason.INVALID_AMOUNT)
        }

        val payment = paymentStepExecutor.register(orderId, amount)
        if (payment.status == PaymentStatus.COMPLETED) return PayResult.from(payment)

        val approved = paymentGateway.approve(payment.orderId, payment.amount)
        val settled = paymentStepExecutor.settle(payment.id, approved)
        return when (settled.status) {
            PaymentStatus.COMPLETED -> PayResult.from(settled)
            PaymentStatus.REJECTED -> throw rejected(orderId, PaymentRejectionReason.GATEWAY_DECLINED)
            PaymentStatus.CANCELLED -> {

                log.warn("승인을 기다리는 사이 취소된 결제입니다. 받은 승인을 취소합니다. orderId={}, paymentId={}, approved={}", orderId, settled.id, approved)
                if (approved) paymentGateway.cancel(settled)
                throw rejected(orderId, PaymentRejectionReason.ORDER_CANCELLED)
            }
            PaymentStatus.PENDING -> error("승인 결과가 반영되지 않았습니다. paymentId=${settled.id}")
        }
    }

    private fun rejected(orderId: OrderId, reason: PaymentRejectionReason): PaymentRejectedException {
        log.info("주문의 결제를 거절했습니다. orderId={}, reason={}", orderId, reason)
        return PaymentRejectedException(orderId.toString(), reason)
    }
}
