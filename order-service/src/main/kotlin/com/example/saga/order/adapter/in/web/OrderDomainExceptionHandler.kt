package com.example.saga.order.adapter.`in`.web

import com.example.saga.order.domain.exception.DuplicateOrderItemException
import com.example.saga.order.domain.exception.DuplicateOrderItemIdException
import com.example.saga.order.domain.exception.InvalidOrderItemStatusTransitionException
import com.example.saga.order.domain.exception.InvalidOrderQuantityException
import com.example.saga.order.domain.exception.InvalidOrderStatusTransitionException
import com.example.saga.order.domain.exception.NegativeAmountException
import com.example.saga.order.domain.exception.OrderAmountOverflowException
import com.example.saga.order.domain.exception.OrderDomainException
import com.example.saga.order.domain.exception.OrderItemChangeNotAllowedException
import com.example.saga.order.domain.exception.OrderItemNotFoundException
import com.example.saga.order.domain.exception.OrderItemsLimitExceededException
import com.example.saga.order.domain.exception.OrderItemsRequiredException
import com.example.saga.platform.web.ProblemDetailFactory
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(OrderDomainExceptionHandler::class.java)
private const val NO_DIAGNOSTICS = "-"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderDomainExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(OrderDomainException::class)
    fun handle(exception: OrderDomainException): ProblemDetail = when (exception) {
        is InvalidOrderQuantityException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "INVALID_ORDER_QUANTITY",
                "주문 수량은 1 이상이어야 합니다.",
                "quantity=${exception.quantity}"
            )
        is NegativeAmountException -> when (exception.subject) {
            NegativeAmountException.Subject.UNIT_PRICE ->
                respond(
                    exception,
                    HttpStatus.BAD_REQUEST,
                    "UNIT_PRICE_NEGATIVE",
                    "주문 단가는 음수일 수 없습니다.",
                    "unitPrice=${exception.value}"
                )
            NegativeAmountException.Subject.ORDER_AMOUNT ->
                respond(
                    exception,
                    HttpStatus.BAD_REQUEST,
                    "ORDER_AMOUNT_NEGATIVE",
                    "주문 금액은 음수일 수 없습니다.",
                    "amount=${exception.value}"
                )
        }
        is OrderAmountOverflowException ->
            respond(exception, HttpStatus.BAD_REQUEST, "ORDER_AMOUNT_OVERFLOW", "주문 금액이 허용 범위를 벗어났습니다.")
        is OrderItemsLimitExceededException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "ORDER_ITEMS_LIMIT_EXCEEDED",
                "주문 항목은 ${exception.maxSize}개를 넘을 수 없습니다.",
                "size=${exception.size}, maxSize=${exception.maxSize}"
            )
        is OrderItemsRequiredException ->
            respond(exception, HttpStatus.BAD_REQUEST, "ORDER_ITEMS_REQUIRED", "주문 항목은 1개 이상이어야 합니다.")
        is DuplicateOrderItemException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "DUPLICATE_ORDER_ITEM",
                "같은 재고를 중복해서 주문할 수 없습니다.",
                "stockId=${exception.stockReferenceId}"
            )
        is InvalidOrderStatusTransitionException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "ORDER_STATUS_TRANSITION_NOT_ALLOWED",
                "허용되지 않는 주문 상태 변경입니다.",
                "currentStatus=${exception.currentStatus}, targetStatus=${exception.targetStatus}"
            )

        is DuplicateOrderItemIdException ->
            respond(
                exception,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DUPLICATE_ORDER_ITEM_ID",
                "일시적인 오류가 발생했습니다.",
                "itemId=${exception.itemId}"
            )
        is OrderItemNotFoundException ->
            respond(
                exception,
                HttpStatus.NOT_FOUND,
                "ORDER_ITEM_NOT_FOUND",
                "주문 항목을 찾을 수 없습니다.",
                "itemId=${exception.itemId}"
            )
        is InvalidOrderItemStatusTransitionException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "ORDER_ITEM_STATUS_TRANSITION_NOT_ALLOWED",
                "허용되지 않는 주문 항목 상태 변경입니다.",
                "currentStatus=${exception.currentStatus}, targetStatus=${exception.targetStatus}"
            )
        is OrderItemChangeNotAllowedException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "ORDER_ITEM_CHANGE_NOT_ALLOWED",
                "완료된 주문의 항목만 변경할 수 있습니다.",
                "orderStatus=${exception.orderStatus}"
            )
    }

    private fun respond(
        exception: OrderDomainException,
        status: HttpStatus,
        code: String,
        message: String,
        diagnostics: String = NO_DIAGNOSTICS
    ): ProblemDetail {
        log.warn(
            "Order 도메인 예외가 발생했습니다. type={}, code={}, diagnostics={}, message={}",
            exception::class.simpleName,
            code,
            diagnostics,
            message
        )
        return problemDetailFactory.create(status, code, message)
    }
}
