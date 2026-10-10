package com.example.saga.order.domain.exception

import com.example.saga.order.domain.vo.OrderItemId

class OrderItemNotFoundException(val itemId: OrderItemId) : OrderDomainException()
