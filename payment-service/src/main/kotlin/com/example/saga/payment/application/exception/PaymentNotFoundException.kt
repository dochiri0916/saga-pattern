package com.example.saga.payment.application.exception

class PaymentNotFoundException(val paymentId: String) : PaymentApplicationException("결제를 찾을 수 없습니다.")
