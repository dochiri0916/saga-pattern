package com.example.saga.order.application.exception

class OrderRejectedException(
    val orderId: String,

    val reason: String?,
) : OrderApplicationException("주문을 처리할 수 없어 취소했습니다.")
