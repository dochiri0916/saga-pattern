package com.example.saga.order.adapter.`in`.web.controller

import com.example.saga.order.adapter.`in`.requireV7
import com.example.saga.order.adapter.`in`.web.OrderPlacementHandler
import com.example.saga.order.adapter.`in`.web.mapper.toResponse
import com.example.saga.order.adapter.`in`.web.request.PlaceOrderRequest
import com.example.saga.order.adapter.`in`.web.response.CancelOrderResponse
import com.example.saga.order.adapter.`in`.web.response.GetOrderResponse
import com.example.saga.order.adapter.`in`.web.response.PlaceOrderResponse
import com.example.saga.order.application.command.CancelOrderCommand
import com.example.saga.order.application.port.`in`.CancelOrderUseCase
import com.example.saga.order.application.port.`in`.GetOrderUseCase
import com.example.saga.order.application.query.GetOrderQuery
import com.example.saga.order.domain.order.vo.OrderStatus
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderPlacementHandler: OrderPlacementHandler,
    private val getOrderUseCase: GetOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
) {

    @PostMapping
    fun place(
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
        @RequestBody request: PlaceOrderRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        val response = orderPlacementHandler.place(idempotencyKey, request).toResponse()
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{orderId}")
            .buildAndExpand(response.orderId)
            .toUri()

        return if (response.status == OrderStatus.PENDING.name) {
            ResponseEntity.accepted().location(location).body(response)
        } else {
            ResponseEntity.created(location).body(response)
        }
    }

    @GetMapping("/{orderId}")
    fun get(@PathVariable orderId: UUID): GetOrderResponse =
        getOrderUseCase.get(GetOrderQuery(orderId.requireV7("orderId"))).toResponse()

    @PostMapping("/{orderId}/cancel")
    fun cancel(@PathVariable orderId: UUID): ResponseEntity<CancelOrderResponse> {
        val response = cancelOrderUseCase.cancel(CancelOrderCommand(orderId.requireV7("orderId"))).toResponse()
        return if (response.status == OrderStatus.CANCELLED.name) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.accepted().body(response)
        }
    }
}
