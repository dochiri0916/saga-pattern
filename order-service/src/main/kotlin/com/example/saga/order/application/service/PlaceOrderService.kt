package com.example.saga.order.application.service

import com.example.saga.order.application.exception.IdempotencyKeyReusedException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.exception.StockNotFoundException
import com.example.saga.order.application.port.`in`.command.PlaceOrderCommand
import com.example.saga.order.application.port.`in`.command.PlaceOrderItemCommand
import com.example.saga.order.application.port.`in`.result.PlaceOrderResult
import com.example.saga.order.application.port.`in`.usecase.PlaceOrderUseCase
import com.example.saga.order.application.port.out.OrderIdGenerator
import com.example.saga.order.application.port.out.OrderIdempotencyKey
import com.example.saga.order.application.port.out.OrderIdempotencyKeyRepository
import com.example.saga.order.application.port.out.OrderItemIdGenerator
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.saga.OrderSagaStatus
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.OrderItem
import com.example.saga.order.domain.OrderItems
import com.example.saga.order.domain.vo.OrderQuantity
import com.example.saga.order.domain.vo.OrderStatus
import com.example.saga.order.domain.vo.StockReferenceId
import com.example.saga.order.domain.vo.UnitPrice
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.HexFormat

@Service
class PlaceOrderService(
    private val orderIdGenerator: OrderIdGenerator,
    private val orderItemIdGenerator: OrderItemIdGenerator,
    private val orderRepository: OrderRepository,
    private val orderSagaRepository: OrderSagaRepository,
    private val orderIdempotencyKeyRepository: OrderIdempotencyKeyRepository,
    private val stockSnapshotRepository: StockSnapshotRepository,
    private val orderSagaStepExecutor: OrderSagaStepExecutor,
    private val orderSagaDriver: OrderSagaDriver,
) : PlaceOrderUseCase {

    override fun place(command: PlaceOrderCommand): PlaceOrderResult {
        val key = command.idempotencyKey
        val requestHash = key?.let { command.hash() }
        if (key != null) orderIdempotencyKeyRepository.findByKey(key)?.let { return replay(it, requestHash) }

        val items = OrderItems.of(command.items.map { it.toOrderItem() })
        val order = Order.register(orderIdGenerator.generate(), items)

        val started = if (key == null) {
            orderSagaStepExecutor.start(order)
        } else {

            orderSagaStepExecutor.startOnce(order, OrderIdempotencyKey(key, order.id, requestHash!!))
                ?: return replay(orderIdempotencyKeyRepository.findByKey(key) ?: error("저장된 멱등 키를 찾지 못했습니다."), requestHash)
        }

        val saga = orderSagaDriver.drive(order, started)
        if (saga.status == OrderSagaStatus.CANCELLED) throw OrderRejectedException(order.id.toString(), saga.failureReason)

        return PlaceOrderResult.from(orderRepository.findById(order.id) ?: order)
    }

    private fun replay(idempotencyKey: OrderIdempotencyKey, requestHash: String?): PlaceOrderResult {
        if (idempotencyKey.requestHash != requestHash) throw IdempotencyKeyReusedException(idempotencyKey.key)

        val orderId = idempotencyKey.orderId
        val order = orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId.toString())
        if (order.status == OrderStatus.CANCELLED) {
            throw OrderRejectedException(orderId.toString(), orderSagaRepository.findByOrderId(orderId)?.failureReason)
        }
        return PlaceOrderResult.from(order)
    }

    private fun PlaceOrderCommand.hash(): String {
        val canonical = items.map { "${it.stockId}:${it.quantity}" }.sorted().joinToString(",")
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray()))
    }

    private fun PlaceOrderItemCommand.toOrderItem(): OrderItem {
        val stockReferenceId = StockReferenceId.of(stockId)
        val snapshot = stockSnapshotRepository.findById(stockReferenceId) ?: throw StockNotFoundException(stockId.toString())
        return OrderItem.register(orderItemIdGenerator.generate(), stockReferenceId, OrderQuantity.of(quantity), UnitPrice.of(snapshot.price))
    }
}
