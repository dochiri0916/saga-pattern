package com.example.saga.shipping.adapter.`in`.web

import com.example.saga.infrastructure.web.ProblemDetailFactory
import com.example.saga.shipping.adapter.`in`.web.controller.OrderShipmentController
import com.example.saga.shipping.application.exception.ShipmentApplicationException
import com.example.saga.shipping.application.exception.ShipmentCancelRejectedException
import com.example.saga.shipping.application.exception.ShipmentNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(ShipmentApplicationExceptionHandler::class.java)
private const val SHIPMENT_NOT_FOUND = "SHIPMENT_NOT_FOUND"
private const val SHIPMENT_CANCEL_REJECTED = "SHIPMENT_CANCEL_REJECTED"

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderShipmentController::class])
class ShipmentApplicationExceptionHandler(
    private val problemDetailFactory: ProblemDetailFactory
) {

    @ExceptionHandler(ShipmentApplicationException::class)
    fun handle(exception: ShipmentApplicationException): ProblemDetail = when (exception) {
        is ShipmentNotFoundException -> {
            log.info("Shipment 애플리케이션 예외가 발생했습니다. code=$SHIPMENT_NOT_FOUND, orderId={}", exception.orderId)
            problemDetailFactory.create(HttpStatus.NOT_FOUND, SHIPMENT_NOT_FOUND, exception.message)
        }

        is ShipmentCancelRejectedException -> {
            log.info(
                "Shipment 애플리케이션 예외가 발생했습니다. code=$SHIPMENT_CANCEL_REJECTED, orderId={}, reason={}",
                exception.orderId,
                exception.reason
            )
            problemDetailFactory.create(HttpStatus.CONFLICT, SHIPMENT_CANCEL_REJECTED, exception.message).apply {
                setProperty("reason", exception.reason.name)
            }
        }
    }
}
