package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.payment.vo.PaymentId

interface PaymentIdGenerator {

    fun generate(): PaymentId
}
