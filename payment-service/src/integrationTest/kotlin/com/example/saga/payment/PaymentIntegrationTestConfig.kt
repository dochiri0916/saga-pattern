package com.example.saga.payment

import com.example.saga.payment.application.port.out.PaymentGateway
import com.example.saga.payment.domain.payment.Payment
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentAmount
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.saga.payment.application.service",
        "com.example.saga.payment.adapter.out.persistence",
        "com.example.saga.payment.adapter.out.audit",
        "com.example.saga.payment.adapter.out.identifier",
    ]
)
class PaymentIntegrationTestConfig {

    @Bean
    fun paymentGateway(): RecordingPaymentGateway = RecordingPaymentGateway()
}

class RecordingPaymentGateway : PaymentGateway {
    val approved: MutableSet<OrderReferenceId> = ConcurrentHashMap.newKeySet()
    val cancelled: MutableSet<OrderReferenceId> = ConcurrentHashMap.newKeySet()

    override fun approve(orderId: OrderReferenceId, amount: PaymentAmount): Boolean {
        Thread.sleep(ThreadLocalRandom.current().nextLong(0, 5))
        approved += orderId
        return true
    }

    override fun cancel(payment: Payment) {
        cancelled += payment.orderId
    }
}
