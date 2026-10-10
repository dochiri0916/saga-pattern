package com.example.saga.payment.domain.exception

class InvalidPaymentAmountException(val amount: Int) : PaymentDomainException()
