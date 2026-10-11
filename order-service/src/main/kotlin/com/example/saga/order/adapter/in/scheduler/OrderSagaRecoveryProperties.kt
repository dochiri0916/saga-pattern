package com.example.saga.order.adapter.`in`.scheduler

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("order.saga.recovery")
data class OrderSagaRecoveryProperties(

    val after: Duration,
    val batchSize: Int,
)
