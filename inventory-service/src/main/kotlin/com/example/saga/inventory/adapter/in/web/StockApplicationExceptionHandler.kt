package com.example.saga.inventory.adapter.`in`.web

import com.example.saga.inventory.application.exception.OrderStockDecreaseRejectedException
import com.example.saga.inventory.application.exception.StockApplicationException
import com.example.saga.inventory.application.exception.StockConcurrentUpdateException
import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.platform.web.ProblemDetailFactory
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(StockApplicationExceptionHandler::class.java)

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [StockController::class, OrderStockController::class])
class StockApplicationExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(StockApplicationException::class)
    fun handle(exception: StockApplicationException): ProblemDetail = when (exception) {
        is StockNotFoundException -> {
            log.info("Stock 애플리케이션 예외가 발생했습니다. code=STOCK_NOT_FOUND, stockId={}", exception.stockId)
            problemDetailFactory.create(HttpStatus.NOT_FOUND, "STOCK_NOT_FOUND", exception.message)
        }
        is StockConcurrentUpdateException -> {
            log.warn("Stock 애플리케이션 예외가 발생했습니다. code=STOCK_CONCURRENT_UPDATE, stockId={}", exception.stockId)
            problemDetailFactory.create(HttpStatus.CONFLICT, "STOCK_CONCURRENT_UPDATE", exception.message)
        }

        is OrderStockDecreaseRejectedException -> {
            log.info(
                "Stock 애플리케이션 예외가 발생했습니다. code=STOCK_DECREASE_REJECTED, orderId={}, reason={}, stockId={}",
                exception.orderId,
                exception.reason,
                exception.stockId
            )
            problemDetailFactory.create(HttpStatus.CONFLICT, "STOCK_DECREASE_REJECTED", exception.message).apply {
                setProperty("reason", exception.reason.name)
                setProperty("stockId", exception.stockId)
            }
        }
    }
}
