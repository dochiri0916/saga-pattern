package com.example.saga.infrastructure.messaging.inbox

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("saga.inbox.cleanup")
data class InboxCleanupProperties(
    val retention: Duration,
    val batchSize: Int,
)
