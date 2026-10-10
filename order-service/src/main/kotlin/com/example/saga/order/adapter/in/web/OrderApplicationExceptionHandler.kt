package com.example.saga.order.adapter.`in`.web

import com.example.saga.order.application.exception.IdempotencyKeyReusedException
import com.example.saga.order.application.exception.OrderApplicationException
import com.example.saga.order.application.exception.OrderCancelRejectedException
import com.example.saga.order.application.exception.OrderConcurrentUpdateException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.exception.OrderSagaNotFoundException
import com.example.saga.order.application.exception.StockNotFoundException
import com.example.saga.platform.web.ProblemDetailFactory
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(OrderApplicationExceptionHandler::class.java)

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderApplicationExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(OrderApplicationException::class)
    fun handle(exception: OrderApplicationException): ProblemDetail = when (exception) {
        is OrderNotFoundException -> {
            log.info("Order 애플리케이션 예외가 발생했습니다. code=ORDER_NOT_FOUND, orderId={}", exception.orderId)
            problemDetailFactory.create(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", exception.message)
        }
        is OrderSagaNotFoundException -> {
            log.warn("Order 애플리케이션 예외가 발생했습니다. code=ORDER_SAGA_NOT_FOUND, orderId={}", exception.orderId)
            problemDetailFactory.create(HttpStatus.NOT_FOUND, "ORDER_SAGA_NOT_FOUND", exception.message)
        }
        is StockNotFoundException -> {
            log.info("Order 애플리케이션 예외가 발생했습니다. code=STOCK_NOT_FOUND, stockId={}", exception.stockId)
            problemDetailFactory.create(HttpStatus.NOT_FOUND, "STOCK_NOT_FOUND", exception.message)
        }
        is OrderConcurrentUpdateException -> {
            log.warn("Order 애플리케이션 예외가 발생했습니다. code=ORDER_CONCURRENT_UPDATE, orderId={}", exception.orderId)
            problemDetailFactory.create(HttpStatus.CONFLICT, "ORDER_CONCURRENT_UPDATE", exception.message)
        }
        is IdempotencyKeyReusedException -> {
            log.info("Order 애플리케이션 예외가 발생했습니다. code=IDEMPOTENCY_KEY_REUSED, idempotencyKey={}", exception.idempotencyKey)
            problemDetailFactory.create(HttpStatus.UNPROCESSABLE_CONTENT, "IDEMPOTENCY_KEY_REUSED", exception.message)
        }

        is OrderCancelRejectedException -> {
            log.info(
                "Order 애플리케이션 예외가 발생했습니다. code=ORDER_CANCEL_REJECTED, orderId={}, reason={}",
                exception.orderId,
                exception.reason
            )
            problemDetailFactory.create(HttpStatus.CONFLICT, "ORDER_CANCEL_REJECTED", exception.message).apply {
                setProperty("orderId", exception.orderId)
                setProperty("reason", exception.reason)
            }
        }

        is OrderRejectedException -> {
            log.info("Order 애플리케이션 예외가 발생했습니다. code=ORDER_REJECTED, orderId={}, reason={}", exception.orderId, exception.reason)
            problemDetailFactory.create(HttpStatus.CONFLICT, "ORDER_REJECTED", exception.message).apply {
                setProperty("orderId", exception.orderId)
                setProperty("reason", exception.reason)
            }
        }
    }
}
