package com.example.saga.order.application.service

import com.example.saga.order.application.command.PlaceOrderCommand
import com.example.saga.order.application.command.PlaceOrderItemCommand
import com.example.saga.order.application.exception.StockNotFoundException
import com.example.saga.order.application.port.`in`.StartOrderUseCase
import com.example.saga.order.application.port.out.OrderIdGenerator
import com.example.saga.order.application.port.out.OrderItemIdGenerator
import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.result.StartedOrderResult
import com.example.saga.order.application.saga.orchestrator.OrderSagaStepExecutor
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.OrderItem
import com.example.saga.order.domain.order.OrderItems
import com.example.saga.order.domain.order.vo.OrderQuantity
import com.example.saga.order.domain.order.vo.StockReferenceId
import com.example.saga.order.domain.order.vo.UnitPrice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StartOrderService(
    private val orderIdGenerator: OrderIdGenerator,
    private val orderItemIdGenerator: OrderItemIdGenerator,
    private val stockSnapshotRepository: StockSnapshotRepository,
    private val executor: OrderSagaStepExecutor,
) : StartOrderUseCase {
    @Transactional
    override fun start(command: PlaceOrderCommand): StartedOrderResult {
        val order = Order.register(orderIdGenerator.generate(), OrderItems.of(command.items.map { it.toOrderItem() }))
        executor.start(order)
        return StartedOrderResult(order.id.value)
    }

    private fun PlaceOrderItemCommand.toOrderItem(): OrderItem {
        val reference = StockReferenceId.of(stockId)
        val snapshot = stockSnapshotRepository.findById(reference) ?: throw StockNotFoundException(stockId.toString())
        return OrderItem.register(orderItemIdGenerator.generate(), reference, OrderQuantity.of(quantity), UnitPrice.of(snapshot.price))
    }
}
