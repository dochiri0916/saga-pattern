package com.example.saga.platform.idempotency

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("saga.processed-events.cleanup")
data class ProcessedEventCleanupProperties(
    val retention: Duration,
    val batchSize: Int,
)
