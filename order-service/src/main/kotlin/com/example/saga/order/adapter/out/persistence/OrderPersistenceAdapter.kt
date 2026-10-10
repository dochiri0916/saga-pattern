package com.example.saga.order.adapter.out.persistence

import com.example.saga.order.application.exception.OrderConcurrentUpdateException
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.OrderItem
import com.example.saga.order.domain.OrderItems
import com.example.saga.order.domain.vo.OrderId
import com.example.saga.order.domain.vo.OrderItemId
import com.example.saga.order.domain.vo.OrderItemStatus
import com.example.saga.order.domain.vo.OrderQuantity
import com.example.saga.order.domain.vo.OrderStatus
import com.example.saga.order.domain.vo.StockReferenceId
import com.example.saga.order.domain.vo.UnitPrice
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
class OrderPersistenceAdapter(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = orderJpaRepository.findByPublicId(order.id.value)
            ?.apply { update(order) }
            ?: order.toEntity()

        try {
            orderJpaRepository.saveAndFlush(entity)
        } catch (e: OptimisticLockingFailureException) {
            throw OrderConcurrentUpdateException(order.id.toString(), e)
        }
        return order
    }

    override fun findById(id: OrderId): Order? =
        orderJpaRepository.findByPublicId(id.value)?.toDomain()

    private fun OrderEntity.update(order: Order) {
        updateStatus(order.status.name)
        val statuses = order.items.associate { it.id.value to it.status.name }
        items.forEach { item -> statuses[item.publicId]?.let(item::updateStatus) }
    }

    private fun Order.toEntity(): OrderEntity = OrderEntity.of(
        publicId = id.value,
        status = status.name,
        items = items.map {
            OrderItemEntity.of(it.id.value, it.stockReferenceId.value, it.quantity.value, it.unitPrice.value, it.status.name)
        },
    )

    private fun OrderEntity.toDomain(): Order = Order.restore(
        id = OrderId.of(publicId),
        items = OrderItems.of(
            items.map {
                OrderItem.restore(
                    id = OrderItemId.of(it.publicId),
                    stockReferenceId = StockReferenceId.of(it.stockId),
                    quantity = OrderQuantity.of(it.quantity),
                    unitPrice = UnitPrice.of(it.unitPrice),
                    status = OrderItemStatus.valueOf(it.status),
                )
            }
        ),
        status = OrderStatus.valueOf(status),
    )
}
