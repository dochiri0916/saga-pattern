package com.example.saga.inventory

import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRevision
import com.example.saga.inventory.domain.Stock
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
        "com.example.saga.inventory.adapter.out.identifier",
    ]
)
class InventoryIntegrationTestConfig {

    @Bean
    fun stockEventPublisher(): StockEventPublisher = object : StockEventPublisher {
        override fun publishStockChanged(stock: Stock, revision: StockRevision) = Unit
    }
}
