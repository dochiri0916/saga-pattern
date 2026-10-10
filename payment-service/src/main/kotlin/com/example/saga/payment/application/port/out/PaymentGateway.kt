package com.example.saga.payment.application.port.out

import com.example.saga.payment.domain.Payment
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount

interface PaymentGateway {

    fun approve(orderId: OrderId, amount: PaymentAmount): Boolean

    fun cancel(payment: Payment)
}
