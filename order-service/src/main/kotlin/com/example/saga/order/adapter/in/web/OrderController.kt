package com.example.saga.order.adapter.`in`.web

import com.example.saga.order.adapter.`in`.web.request.PlaceOrderRequest
import com.example.saga.order.adapter.`in`.web.response.CancelOrderResponse
import com.example.saga.order.adapter.`in`.web.response.GetOrderResponse
import com.example.saga.order.adapter.`in`.web.response.PlaceOrderResponse
import com.example.saga.order.adapter.`in`.requireV7
import com.example.saga.order.application.port.`in`.command.CancelOrderCommand
import com.example.saga.order.application.port.`in`.query.GetOrderQuery
import com.example.saga.order.application.port.`in`.usecase.CancelOrderUseCase
import com.example.saga.order.application.port.`in`.usecase.GetOrderUseCase
import com.example.saga.order.application.port.`in`.usecase.PlaceOrderUseCase
import com.example.saga.order.domain.vo.OrderStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getOrderUseCase: GetOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
) {

    @PostMapping
    fun place(
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
        @RequestBody request: PlaceOrderRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        val response = PlaceOrderResponse.from(placeOrderUseCase.place(request.toCommand(idempotencyKey)))
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
        GetOrderResponse.from(getOrderUseCase.get(GetOrderQuery(orderId.requireV7("orderId"))))

    @PostMapping("/{orderId}/cancel")
    fun cancel(@PathVariable orderId: UUID): ResponseEntity<CancelOrderResponse> {
        val response = CancelOrderResponse.from(cancelOrderUseCase.cancel(CancelOrderCommand(orderId.requireV7("orderId"))))
        return if (response.status == OrderStatus.CANCELLED.name) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.accepted().body(response)
        }
    }
}
