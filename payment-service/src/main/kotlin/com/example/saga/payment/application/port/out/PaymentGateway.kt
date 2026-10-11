package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.payment.Payment
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentAmount

interface PaymentGateway {

    fun approve(orderId: OrderReferenceId, amount: PaymentAmount): Boolean

    fun cancel(payment: Payment)
}
