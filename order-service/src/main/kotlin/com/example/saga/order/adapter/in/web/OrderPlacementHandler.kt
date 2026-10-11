package com.example.saga.order.adapter.`in`.web

import com.example.saga.infrastructure.idempotency.IdempotencyInterceptor
import com.example.saga.order.adapter.`in`.web.mapper.PlaceOrderRequestMapper
import com.example.saga.order.adapter.`in`.web.request.PlaceOrderRequest
import com.example.saga.order.application.command.CompleteOrderPlacementCommand
import com.example.saga.order.application.port.`in`.CompleteOrderPlacementUseCase
import com.example.saga.order.application.port.`in`.GetOrderPlacementUseCase
import com.example.saga.order.application.port.`in`.StartOrderUseCase
import com.example.saga.order.application.query.GetOrderPlacementQuery
import com.example.saga.order.application.result.PlaceOrderResult
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class OrderPlacementHandler(
    private val startOrderUseCase: StartOrderUseCase,
    private val completeOrderPlacementUseCase: CompleteOrderPlacementUseCase,
    private val getOrderPlacementUseCase: GetOrderPlacementUseCase,
    private val interceptor: IdempotencyInterceptor,
) {
    fun place(key: String?, request: PlaceOrderRequest): PlaceOrderResult {
        val command = PlaceOrderRequestMapper.toCommand(request)
        return interceptor.execute(
            key, "POST /orders", PlaceOrderRequestMapper.canonicalRequest(request),
            prepare = { startOrderUseCase.start(command).orderId.toString() },
            onCreated = { completeOrderPlacementUseCase.complete(CompleteOrderPlacementCommand(UUID.fromString(it))) },
            onReplay = { getOrderPlacementUseCase.placement(GetOrderPlacementQuery(UUID.fromString(it))) },
        )
    }
}
