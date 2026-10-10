package com.example.saga.payment.application.exception

class PaymentAlreadyExistsException(val orderId: String, cause: Throwable? = null) :
    PaymentApplicationException("이미 결제가 존재하는 주문입니다.", cause)
