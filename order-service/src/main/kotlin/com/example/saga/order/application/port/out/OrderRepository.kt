package com.example.saga.order.application.port.out

import com.example.saga.order.domain.Order
import com.example.saga.order.domain.vo.OrderId

interface OrderRepository {

    fun save(order: Order): Order

    fun findById(id: OrderId): Order?
}
