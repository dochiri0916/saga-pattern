package com.example.saga.order.domain.exception

import com.example.saga.order.domain.vo.OrderStatus

class InvalidOrderStatusTransitionException(val currentStatus: OrderStatus, val targetStatus: OrderStatus) :
    OrderDomainException()
