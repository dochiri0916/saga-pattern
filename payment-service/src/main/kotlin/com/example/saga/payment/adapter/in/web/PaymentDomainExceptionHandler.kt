package com.example.saga.payment.adapter.`in`.web

import com.example.saga.infrastructure.web.ProblemDetailFactory
import com.example.saga.payment.adapter.`in`.web.controller.OrderPaymentController
import com.example.saga.payment.adapter.`in`.web.controller.PaymentController
import com.example.saga.payment.domain.payment.exception.InvalidPaymentAmountException
import com.example.saga.payment.domain.payment.exception.InvalidPaymentStatusTransitionException
import com.example.saga.payment.domain.payment.exception.PaymentDomainException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(PaymentDomainExceptionHandler::class.java)
private const val NO_DIAGNOSTICS = "-"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [PaymentController::class, OrderPaymentController::class])
class PaymentDomainExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(PaymentDomainException::class)
    fun handle(exception: PaymentDomainException): ProblemDetail = when (exception) {
        is InvalidPaymentAmountException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_AMOUNT",
                "결제 금액은 1 이상이어야 합니다.",
                "amount=${exception.amount}"
            )
        is InvalidPaymentStatusTransitionException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "PAYMENT_STATUS_TRANSITION_NOT_ALLOWED",
                "허용되지 않는 결제 상태 변경입니다.",
                "currentStatus=${exception.currentStatus}, targetStatus=${exception.targetStatus}"
            )
    }

    private fun respond(
        exception: PaymentDomainException,
        status: HttpStatus,
        code: String,
        message: String,
        diagnostics: String = NO_DIAGNOSTICS
    ): ProblemDetail {
        log.warn(
            "Payment 도메인 예외가 발생했습니다. type={}, code={}, diagnostics={}, message={}",
            exception::class.simpleName,
            code,
            diagnostics,
            message
        )
        return problemDetailFactory.create(status, code, message)
    }
}
