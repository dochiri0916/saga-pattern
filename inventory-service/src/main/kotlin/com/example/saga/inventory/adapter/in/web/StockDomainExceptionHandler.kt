package com.example.saga.inventory.adapter.`in`.web

import com.example.saga.inventory.domain.exception.InsufficientStockException
import com.example.saga.inventory.domain.exception.InvalidStockAmountException
import com.example.saga.inventory.domain.exception.NegativeStockPriceException
import com.example.saga.inventory.domain.exception.NegativeStockQuantityException
import com.example.saga.inventory.domain.exception.StockDomainException
import com.example.saga.inventory.domain.exception.StockNameRequiredException
import com.example.saga.inventory.domain.exception.StockOverflowException
import com.example.saga.platform.web.ProblemDetailFactory
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(StockDomainExceptionHandler::class.java)
private const val NO_DIAGNOSTICS = "-"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [StockController::class, OrderStockController::class])
class StockDomainExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(StockDomainException::class)
    fun handle(exception: StockDomainException): ProblemDetail = when (exception) {
        is StockNameRequiredException ->
            respond(exception, HttpStatus.BAD_REQUEST, "STOCK_NAME_REQUIRED", "상품명은 필수입니다.")

        is NegativeStockQuantityException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "STOCK_QUANTITY_NEGATIVE",
                "수량은 음수일 수 없습니다.",
                "quantity=${exception.quantity}"
            )

        is NegativeStockPriceException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "STOCK_PRICE_NEGATIVE",
                "가격은 음수일 수 없습니다.",
                "price=${exception.price}"
            )

        is InvalidStockAmountException ->
            respond(
                exception,
                HttpStatus.BAD_REQUEST,
                "INVALID_STOCK_AMOUNT",
                "재고 변경 수량은 1 이상이어야 합니다.",
                "amount=${exception.amount}"
            )

        is InsufficientStockException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "STOCK_INSUFFICIENT",
                "재고가 부족합니다. 현재 재고는 ${exception.currentStock}개이고 요청 수량은 ${exception.requestedQuantity}개입니다.",
                "currentStock=${exception.currentStock}, requestedQuantity=${exception.requestedQuantity}"
            )

        is StockOverflowException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "STOCK_OVERFLOW",
                "재고가 허용된 최대 수량을 초과합니다.",
                "currentStock=${exception.currentStock}, requestedQuantity=${exception.requestedQuantity}"
            )
    }

    private fun respond(
        exception: StockDomainException,
        status: HttpStatus,
        code: String,
        message: String,
        diagnostics: String = NO_DIAGNOSTICS
    ): ProblemDetail {
        log.warn(
            "Stock 도메인 예외가 발생했습니다. type={}, code={}, diagnostics={}, message={}",
            exception::class.simpleName,
            code,
            diagnostics,
            message
        )
        return problemDetailFactory.create(status, code, message)
    }
}
