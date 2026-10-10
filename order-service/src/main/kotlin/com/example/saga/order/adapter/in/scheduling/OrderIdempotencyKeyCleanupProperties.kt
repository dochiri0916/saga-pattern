package com.example.saga.order.adapter.`in`.scheduling

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("order.idempotency-keys.cleanup")
data class OrderIdempotencyKeyCleanupProperties(
    val retention: Duration,
    val batchSize: Int,
)
