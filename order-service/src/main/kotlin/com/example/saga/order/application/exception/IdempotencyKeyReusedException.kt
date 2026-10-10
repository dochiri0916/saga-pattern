package com.example.saga.order.application.exception

class IdempotencyKeyReusedException(
    val idempotencyKey: String,
) : OrderApplicationException("이미 다른 주문 요청에 사용한 멱등 키입니다.")
