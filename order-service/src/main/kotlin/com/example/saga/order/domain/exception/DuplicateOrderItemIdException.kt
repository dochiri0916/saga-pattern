package com.example.saga.order.domain.exception

import com.example.saga.order.domain.vo.OrderItemId

class DuplicateOrderItemIdException(val itemId: OrderItemId) : OrderDomainException()
