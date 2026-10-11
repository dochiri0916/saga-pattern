package com.example.saga.order.adapter.`in`.web

import com.example.saga.infrastructure.web.ProblemDetailFactory
import com.example.saga.order.adapter.`in`.web.controller.OrderController
import com.example.saga.order.application.exception.OrderApplicationException
import com.example.saga.order.application.exception.OrderCancelPeriodExpiredException
import com.example.saga.order.application.exception.OrderCancelRejectedException
import com.example.saga.order.application.exception.OrderConcurrentUpdateException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.exception.OrderSagaNotFoundException
import com.example.saga.order.application.exception.StockNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(OrderApplicationExceptionHandler::class.java)
private const val ORDER_NOT_FOUND = "ORDER_NOT_FOUND"
private const val ORDER_SAGA_NOT_FOUND = "ORDER_SAGA_NOT_FOUND"
private const val STOCK_NOT_FOUND = "STOCK_NOT_FOUND"
private const val ORDER_CONCURRENT_UPDATE = "ORDER_CONCURRENT_UPDATE"
private const val ORDER_REJECTED = "ORDER_REJECTED"
private const val ORDER_CANCEL_REJECTED = "ORDER_CANCEL_REJECTED"
private const val CANCEL_PERIOD_EXPIRED = "CANCEL_PERIOD_EXPIRED"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderApplicationExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(OrderApplicationException::class)
    fun handle(exception: OrderApplicationException): ProblemDetail = when (exception) {
        is OrderNotFoundException -> {
            log.info(
                "Order 애플리케이션 예외가 발생했습니다. code=$ORDER_NOT_FOUND, orderId={}",
                exception.orderId
            )
            problemDetailFactory.create(
                HttpStatus.NOT_FOUND,
                ORDER_NOT_FOUND,
                "주문을 찾을 수 없습니다."
            )
        }

        is OrderSagaNotFoundException -> {
            log.warn(
                "Order 애플리케이션 예외가 발생했습니다. code=$ORDER_SAGA_NOT_FOUND, orderId={}",
                exception.orderId
            )
            problemDetailFactory.create(
                HttpStatus.NOT_FOUND,
                ORDER_SAGA_NOT_FOUND,
                "주문의 사가를 찾을 수 없습니다."
            )
        }

        is StockNotFoundException -> {
            log.info(
                "Order 애플리케이션 예외가 발생했습니다. code=$STOCK_NOT_FOUND, stockId={}",
                exception.stockId
            )
            problemDetailFactory.create(
                HttpStatus.NOT_FOUND,
                STOCK_NOT_FOUND,
                "주문할 수 없는 재고입니다."
            )
        }

        is OrderConcurrentUpdateException -> {
            log.warn(
                "Order 애플리케이션 예외가 발생했습니다. code=$ORDER_CONCURRENT_UPDATE, orderId={}",
                exception.orderId
            )
            problemDetailFactory.create(
                HttpStatus.CONFLICT,
                ORDER_CONCURRENT_UPDATE,
                "다른 요청이 동시에 주문을 변경했습니다. 다시 시도해주세요."
            )
        }

        is OrderCancelRejectedException ->
            cancelRejected(exception.orderId, exception.reason)

        is OrderCancelPeriodExpiredException ->
            cancelRejected(exception.orderId, CANCEL_PERIOD_EXPIRED)

        is OrderRejectedException -> {
            log.info(
                "Order 애플리케이션 예외가 발생했습니다. code=$ORDER_REJECTED, orderId={}, reason={}",
                exception.orderId,
                exception.reason
            )
            problemDetailFactory.create(
                HttpStatus.CONFLICT,
                ORDER_REJECTED,
                "주문을 처리할 수 없어 취소했습니다."
            ).apply {
                setProperty("orderId", exception.orderId)
                setProperty("reason", exception.reason)
            }
        }
    }

    private fun cancelRejected(
        orderId: String,
        reason: String?
    ): ProblemDetail {
        log.info(
            "Order 애플리케이션 예외가 발생했습니다. code=$ORDER_CANCEL_REJECTED, orderId={}, reason={}",
            orderId,
            reason
        )
        return problemDetailFactory.create(
            HttpStatus.CONFLICT,
            ORDER_CANCEL_REJECTED,
            "주문을 취소할 수 없습니다."
        ).apply {
            setProperty("orderId", orderId)
            setProperty("reason", reason)
        }
    }
}
