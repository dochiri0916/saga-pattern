package com.example.saga.order

import com.example.saga.order.application.port.out.InventoryClient
import com.example.saga.order.application.port.out.OrderEventPublisher
import com.example.saga.order.application.port.out.PaymentClient
import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.vo.OrderId
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.saga.order.application.service",
        "com.example.saga.order.config",
        "com.example.saga.order.adapter.out.persistence",
        "com.example.saga.order.adapter.out.identifier",
    ]
)
class OrderIntegrationTestConfig {

    @Bean
    fun participants(): StubParticipants = StubParticipants()

    @Bean
    fun orderEventPublisher(): OrderEventPublisher = object : OrderEventPublisher {
        override fun publish(order: Order) = Unit
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
