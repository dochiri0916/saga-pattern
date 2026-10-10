package com.example.saga.order.application.service

import com.example.saga.order.application.exception.OrderCancelRejectedException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.port.`in`.command.CancelOrderCommand
import com.example.saga.order.application.port.`in`.result.CancelOrderResult
import com.example.saga.order.application.port.`in`.usecase.CancelOrderUseCase
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.saga.OrderSagaStatus
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.vo.OrderId
import org.springframework.stereotype.Service

@Service
class CancelOrderService(
    private val orderRepository: OrderRepository,
    private val orderSagaStepExecutor: OrderSagaStepExecutor,
    private val orderSagaDriver: OrderSagaDriver,
) : CancelOrderUseCase {

    override fun cancel(command: CancelOrderCommand): CancelOrderResult {
        val orderId = OrderId.of(command.orderId)
        val order = find(orderId)

        val saga = orderSagaDriver.drive(order, orderSagaStepExecutor.cancel(orderId))
        if (saga.status == OrderSagaStatus.COMPLETED) throw OrderCancelRejectedException(command.orderId.toString(), saga.failureReason)
        return CancelOrderResult.from(find(orderId))
    }

    private fun find(orderId: OrderId): Order =
        orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId.toString())
}
