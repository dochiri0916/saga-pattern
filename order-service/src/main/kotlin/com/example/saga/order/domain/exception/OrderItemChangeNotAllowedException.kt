package com.example.saga.order.domain.exception

import com.example.saga.order.domain.vo.OrderStatus

class OrderItemChangeNotAllowedException(val orderStatus: OrderStatus) : OrderDomainException()
