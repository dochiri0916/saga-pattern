package com.example.saga.order.application.service

import com.example.saga.order.application.command.CompleteOrderPlacementCommand
import com.example.saga.order.application.command.PlaceOrderCommand
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.exception.OrderSagaNotFoundException
import com.example.saga.order.application.port.`in`.CompleteOrderPlacementUseCase
import com.example.saga.order.application.port.`in`.GetOrderPlacementUseCase
import com.example.saga.order.application.port.`in`.PlaceOrderUseCase
import com.example.saga.order.application.port.`in`.StartOrderUseCase
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.query.GetOrderPlacementQuery
import com.example.saga.order.application.result.PlaceOrderResult
import com.example.saga.order.application.saga.orchestrator.OrderSagaDriver
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.OrderStatus
import org.springframework.stereotype.Service

@Service
class PlaceOrderService(
    private val startOrderUseCase: StartOrderUseCase,
    private val orderRepository: OrderRepository,
    private val orderSagaRepository: OrderSagaRepository,
    private val driver: OrderSagaDriver,
) : PlaceOrderUseCase, CompleteOrderPlacementUseCase, GetOrderPlacementUseCase {
    override fun place(command: PlaceOrderCommand): PlaceOrderResult =
        complete(CompleteOrderPlacementCommand(startOrderUseCase.start(command).orderId))

    override fun complete(command: CompleteOrderPlacementCommand): PlaceOrderResult {
        val orderId = OrderId.of(command.orderId)
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId.toString())
        val saga = orderSagaRepository.findByOrderId(orderId) ?: throw OrderSagaNotFoundException(orderId.toString())
        driver.drive(order, saga)
        return placement(GetOrderPlacementQuery(command.orderId))
    }

    override fun placement(query: GetOrderPlacementQuery): PlaceOrderResult {
        val orderId = OrderId.of(query.orderId)
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId.toString())
        if (order.status == OrderStatus.CANCELLED) {
            throw OrderRejectedException(orderId.toString(), orderSagaRepository.findByOrderId(orderId)?.failureReason)
        }
        return PlaceOrderResult.from(order)
    }
}
