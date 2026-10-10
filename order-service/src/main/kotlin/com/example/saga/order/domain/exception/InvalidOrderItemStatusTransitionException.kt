package com.example.saga.order.domain.exception

import com.example.saga.order.domain.vo.OrderItemStatus

class InvalidOrderItemStatusTransitionException(val currentStatus: OrderItemStatus, val targetStatus: OrderItemStatus) :
    OrderDomainException()
