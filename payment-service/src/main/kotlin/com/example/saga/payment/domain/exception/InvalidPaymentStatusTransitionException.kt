package com.example.saga.payment.domain.exception

import com.example.saga.payment.domain.vo.PaymentStatus

class InvalidPaymentStatusTransitionException(val currentStatus: PaymentStatus, val targetStatus: PaymentStatus) : PaymentDomainException()
