package com.example.saga.order.application.port.out

import com.example.saga.order.domain.order.Order

interface PaymentClient {

    fun pay(order: Order): String?

    fun cancelPayment(order: Order)
}
