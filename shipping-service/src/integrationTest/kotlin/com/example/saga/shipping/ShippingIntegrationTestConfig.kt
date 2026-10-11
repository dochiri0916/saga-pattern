package com.example.saga.shipping

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.saga.shipping.application.service",
        "com.example.saga.shipping.adapter.out.persistence",
        "com.example.saga.shipping.adapter.out.audit",
        "com.example.saga.shipping.adapter.out.identifier",
    ]
)
class ShippingIntegrationTestConfig
