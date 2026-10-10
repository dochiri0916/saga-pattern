package com.example.saga.payment.application.exception

class PaymentConcurrentUpdateException(val paymentId: String, cause: Throwable? = null) :
    PaymentApplicationException("다른 요청이 동시에 결제를 변경했습니다. 다시 시도해주세요.", cause)
