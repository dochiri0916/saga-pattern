package com.example.saga.order.domain.order.policy

import java.time.Duration
import java.time.Instant

data class OrderCancelPolicy(
    val allowedFor: Duration,
) {
    fun expired(orderedAt: Instant, now: Instant): Boolean = !now.isBefore(orderedAt.plus(allowedFor))
}
