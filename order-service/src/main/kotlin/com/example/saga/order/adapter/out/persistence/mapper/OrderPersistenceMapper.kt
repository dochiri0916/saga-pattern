package com.example.saga.order.adapter.out.persistence.mapper

import com.example.saga.order.adapter.out.persistence.entity.OrderEntity
import com.example.saga.order.adapter.out.persistence.entity.OrderItemEntity
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.OrderItem
import com.example.saga.order.domain.order.OrderItems
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.OrderItemId
import com.example.saga.order.domain.order.vo.OrderItemStatus
import com.example.saga.order.domain.order.vo.OrderQuantity
import com.example.saga.order.domain.order.vo.OrderStatus
import com.example.saga.order.domain.order.vo.StockReferenceId
import com.example.saga.order.domain.order.vo.UnitPrice

fun OrderEntity.update(order: Order) {
    updateStatus(order.status.name)
    val statuses = order.items.associate { it.id.value to it.status.name }
    items.forEach { item -> statuses[item.publicId]?.let(item::updateStatus) }
}

fun Order.toEntity(): OrderEntity = OrderEntity.of(
    publicId = id.value,
    status = status.name,
    items = items.map {
        OrderItemEntity.of(it.id.value, it.stockReferenceId.value, it.quantity.value, it.unitPrice.value, it.status.name)
    },
)

fun OrderEntity.toDomain(): Order = Order.restore(
    id = OrderId.of(publicId),
    orderItems = OrderItems.of(
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
