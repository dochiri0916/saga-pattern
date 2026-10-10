package com.example.saga.order.adapter.`in`.scheduling

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("order.saga.recovery")
data class OrderSagaRecoveryProperties(

    val after: Duration,
    val batchSize: Int,
)
