package com.example.saga.payment.domain.payment.exception

import com.example.saga.payment.domain.payment.vo.PaymentStatus

sealed class PaymentDomainException(cause: Throwable? = null) : RuntimeException(cause)

class InvalidPaymentAmountException(val amount: Int) : PaymentDomainException()

class InvalidPaymentStatusTransitionException(val currentStatus: PaymentStatus, val targetStatus: PaymentStatus) :
    PaymentDomainException()
