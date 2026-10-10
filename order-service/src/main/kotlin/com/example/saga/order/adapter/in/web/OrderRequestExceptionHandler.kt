package com.example.saga.order.adapter.`in`.web

import com.example.saga.order.adapter.`in`.InvalidUuidV7Exception
import com.example.saga.order.adapter.`in`.web.request.InvalidIdempotencyKeyException
import com.example.saga.platform.web.ProblemDetailFactory
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(OrderRequestExceptionHandler::class.java)

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderRequestExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(InvalidUuidV7Exception::class)
    fun handle(exception: InvalidUuidV7Exception): ProblemDetail {
        log.info("요청의 ID 형식이 유효하지 않습니다. code=INVALID_ID, field={}, value={}", exception.field, exception.value)
        return problemDetailFactory.create(HttpStatus.BAD_REQUEST, "INVALID_ID", exception.message)
    }

    @ExceptionHandler(InvalidIdempotencyKeyException::class)
    fun handle(exception: InvalidIdempotencyKeyException): ProblemDetail {
        log.info("요청의 멱등 키가 유효하지 않습니다. code=INVALID_IDEMPOTENCY_KEY")
        return problemDetailFactory.create(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY", exception.message)
    }
}
