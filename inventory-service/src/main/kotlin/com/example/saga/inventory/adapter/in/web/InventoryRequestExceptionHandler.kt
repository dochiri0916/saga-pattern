package com.example.saga.inventory.adapter.`in`.web

import com.example.saga.infrastructure.web.ProblemDetailFactory
import com.example.saga.inventory.adapter.`in`.InvalidUuidV7Exception
import com.example.saga.inventory.adapter.`in`.web.controller.OrderStockController
import com.example.saga.inventory.adapter.`in`.web.controller.StockController
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(InventoryRequestExceptionHandler::class.java)
private const val INVALID_ID = "INVALID_ID"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderStockController::class, StockController::class])
class InventoryRequestExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(InvalidUuidV7Exception::class)
    fun handle(exception: InvalidUuidV7Exception): ProblemDetail {
        log.info("요청의 ID 형식이 유효하지 않습니다. code=$INVALID_ID, field={}, value={}", exception.field, exception.value)
        return problemDetailFactory.create(HttpStatus.BAD_REQUEST, INVALID_ID, exception.message)
    }
}
