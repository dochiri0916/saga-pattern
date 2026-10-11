package com.example.saga.payment.adapter.`in`.web

import com.example.saga.infrastructure.web.ProblemDetailFactory
import com.example.saga.payment.adapter.`in`.web.controller.OrderPaymentController
import com.example.saga.payment.adapter.`in`.web.controller.PaymentController
import com.example.saga.payment.application.exception.PaymentAlreadyExistsException
import com.example.saga.payment.application.exception.PaymentApplicationException
import com.example.saga.payment.application.exception.PaymentConcurrentUpdateException
import com.example.saga.payment.application.exception.PaymentNotFoundException
import com.example.saga.payment.application.exception.PaymentRejectedException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(PaymentApplicationExceptionHandler::class.java)
private const val PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND"
private const val PAYMENT_ALREADY_EXISTS = "PAYMENT_ALREADY_EXISTS"
private const val PAYMENT_CONCURRENT_UPDATE = "PAYMENT_CONCURRENT_UPDATE"
private const val PAYMENT_REJECTED = "PAYMENT_REJECTED"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [PaymentController::class, OrderPaymentController::class])
class PaymentApplicationExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(PaymentApplicationException::class)
    fun handle(exception: PaymentApplicationException): ProblemDetail = when (exception) {
        is PaymentNotFoundException -> {
            log.info("Payment 애플리케이션 예외가 발생했습니다. code=$PAYMENT_NOT_FOUND, paymentId={}", exception.paymentId)
            problemDetailFactory.create(HttpStatus.NOT_FOUND, PAYMENT_NOT_FOUND, exception.message)
        }
        is PaymentAlreadyExistsException -> {
            log.info("Payment 애플리케이션 예외가 발생했습니다. code=$PAYMENT_ALREADY_EXISTS, orderId={}", exception.orderId)
            problemDetailFactory.create(HttpStatus.CONFLICT, PAYMENT_ALREADY_EXISTS, exception.message)
        }
        is PaymentConcurrentUpdateException -> {
            log.warn("Payment 애플리케이션 예외가 발생했습니다. code=$PAYMENT_CONCURRENT_UPDATE, paymentId={}", exception.paymentId)
            problemDetailFactory.create(HttpStatus.CONFLICT, PAYMENT_CONCURRENT_UPDATE, exception.message)
        }

        is PaymentRejectedException -> {
            log.info(
                "Payment 애플리케이션 예외가 발생했습니다. code=$PAYMENT_REJECTED, orderId={}, reason={}",
                exception.orderId,
                exception.reason
            )
            problemDetailFactory.create(HttpStatus.CONFLICT, PAYMENT_REJECTED, exception.message).apply {
                setProperty("reason", exception.reason.name)
            }
        }
    }
}
