package com.example.saga.payment

import com.example.saga.payment.application.port.out.PaymentGateway
import com.example.saga.payment.domain.Payment
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.saga.payment.application.service",
        "com.example.saga.payment.adapter.out.persistence",
        "com.example.saga.payment.adapter.out.identifier",
    ]
)
class PaymentIntegrationTestConfig {

    @Bean
    fun paymentGateway(): RecordingPaymentGateway = RecordingPaymentGateway()
}

class RecordingPaymentGateway : PaymentGateway {
    val approved: MutableSet<OrderId> = ConcurrentHashMap.newKeySet()
    val cancelled: MutableSet<OrderId> = ConcurrentHashMap.newKeySet()

    override fun approve(orderId: OrderId, amount: PaymentAmount): Boolean {
        Thread.sleep(ThreadLocalRandom.current().nextLong(0, 5))
        approved += orderId
        return true
    }

    override fun cancel(payment: Payment) {
        cancelled += payment.orderId
    }
}
