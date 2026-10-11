package com.example.saga.order.application.port.out

import com.example.saga.order.domain.order.vo.OrderItemId

interface OrderItemIdGenerator {

    fun generate(): OrderItemId
}
