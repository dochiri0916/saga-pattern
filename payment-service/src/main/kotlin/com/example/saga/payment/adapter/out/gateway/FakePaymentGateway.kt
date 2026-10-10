package com.example.saga.payment.adapter.out.gateway

import com.example.saga.payment.application.port.out.PaymentGateway
import com.example.saga.payment.domain.Payment
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(FakePaymentGatewayProperties::class)
class FakePaymentGateway(
    private val properties: FakePaymentGatewayProperties,
) : PaymentGateway {

    override fun approve(orderId: OrderId, amount: PaymentAmount): Boolean {
        if (!properties.approveDelay.isZero) Thread.sleep(properties.approveDelay)
        return amount.value <= properties.rejectOver
    }

    override fun cancel(payment: Payment) = Unit
}
