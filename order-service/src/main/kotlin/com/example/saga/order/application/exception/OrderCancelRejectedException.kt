package com.example.saga.order.application.exception

class OrderCancelRejectedException(
    val orderId: String,

    val reason: String?,
) : OrderApplicationException("주문을 취소할 수 없습니다.")
