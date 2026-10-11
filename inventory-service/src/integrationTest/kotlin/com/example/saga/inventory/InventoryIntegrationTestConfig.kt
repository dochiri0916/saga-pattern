package com.example.saga.inventory

import com.example.saga.inventory.application.port.out.EventPublicationPort
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.saga.inventory.application.service",
        "com.example.saga.inventory.adapter.out.persistence",
        "com.example.saga.inventory.adapter.out.audit",
        "com.example.saga.inventory.adapter.out.identifier",
    ]
)
class InventoryIntegrationTestConfig {

    @Bean
    fun stockEventPublisher(): EventPublicationPort = object : EventPublicationPort {
        override fun publish(event: com.example.saga.inventory.contract.event.StockChangedEvent) = Unit
    }
}
