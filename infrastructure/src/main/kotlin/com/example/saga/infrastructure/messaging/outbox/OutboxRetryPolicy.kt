package com.example.saga.infrastructure.messaging.outbox

import java.time.Duration

class OutboxRetryPolicy(private val properties: OutboxProperties) {
    fun delay(busy: Boolean): Duration = if (busy) Duration.ofMillis(100) else properties.relay.pollInterval
    fun afterFailure(): Duration = properties.relay.pollInterval
}
