package com.example.saga.order

import com.example.saga.order.application.port.out.EventPublicationPort
import com.example.saga.order.application.port.out.InventoryClient
import com.example.saga.order.application.port.out.PaymentClient
import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.vo.OrderId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.saga.order.application.service",
        "com.example.saga.order.application.saga.orchestrator",
        "com.example.saga.order.adapter.in.web",
        "com.example.saga.order.adapter.out.config",
        "com.example.saga.order.adapter.out.persistence",
        "com.example.saga.order.adapter.out.audit",
        "com.example.saga.order.adapter.out.identifier",
    ]
)
class OrderIntegrationTestConfig {

    @Bean
    fun participants(): StubParticipants = StubParticipants()

    @Bean
    fun orderEventPublisher(): EventPublicationPort = object : EventPublicationPort {
        override fun publish(event: com.example.saga.order.contract.event.OrderCompletedEvent) = Unit
    }
}

class StubParticipants : InventoryClient, PaymentClient, ShippingClient {
    private val decreases = ConcurrentHashMap<OrderId, AtomicInteger>()

    fun decreaseCount(orderId: OrderId): Int = decreases[orderId]?.get() ?: 0

    override fun decreaseStock(order: Order): String? {
        decreases.computeIfAbsent(order.id) { AtomicInteger() }.incrementAndGet()
        return null
    }

    override fun restoreStock(order: Order) = Unit
    override fun pay(order: Order): String? = null
    override fun cancelPayment(order: Order) = Unit
    override fun cancelShipment(order: Order): String? = null
}
