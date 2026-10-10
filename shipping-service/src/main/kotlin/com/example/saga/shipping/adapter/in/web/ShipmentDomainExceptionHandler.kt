package com.example.saga.shipping.adapter.`in`.web

import com.example.saga.shipping.domain.exception.InvalidShipmentStatusTransitionException
import com.example.saga.shipping.domain.exception.ShipmentDomainException
import com.example.saga.platform.web.ProblemDetailFactory
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(ShipmentDomainExceptionHandler::class.java)

private const val NO_DIAGNOSTICS = "-"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderShipmentController::class])
class ShipmentDomainExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(ShipmentDomainException::class)
    fun handle(exception: ShipmentDomainException): ProblemDetail = when (exception) {
        is InvalidShipmentStatusTransitionException ->
            respond(
                exception,
                HttpStatus.CONFLICT,
                "SHIPMENT_STATUS_TRANSITION_NOT_ALLOWED",
                "현재 배송 상태에서는 요청한 상태로 변경할 수 없습니다.",
                "currentStatus=${exception.currentStatus}, targetStatus=${exception.targetStatus}"
            )
    }

    private fun respond(
        exception: ShipmentDomainException,
        status: HttpStatus,
        code: String,
        message: String,
        diagnostics: String = NO_DIAGNOSTICS
    ): ProblemDetail {
        log.warn(
            "Shipment 도메인 예외가 발생했습니다. type={}, code={}, diagnostics={}, message={}",
            exception::class.simpleName,
            code,
            diagnostics,
            message
        )
        return problemDetailFactory.create(status, code, message)
    }
}
