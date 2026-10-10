package com.example.saga.order.application.exception

class OrderConcurrentUpdateException(val orderId: String, cause: Throwable? = null) :
    OrderApplicationException("다른 요청이 동시에 주문을 변경했습니다. 다시 시도해주세요.", cause)
