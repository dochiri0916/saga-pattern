package com.example.saga.order.application.exception

class OrderSagaNotFoundException(val orderId: String) : OrderApplicationException("주문의 사가를 찾을 수 없습니다.")
