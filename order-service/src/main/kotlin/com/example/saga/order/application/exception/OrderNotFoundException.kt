package com.example.saga.order.application.exception

class OrderNotFoundException(val orderId: String) : OrderApplicationException("주문을 찾을 수 없습니다.")
