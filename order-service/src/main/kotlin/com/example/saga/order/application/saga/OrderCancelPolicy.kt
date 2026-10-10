package com.example.saga.order.application.saga

import java.time.Duration
import java.time.Instant

data class OrderCancelPolicy(
    val allowedFor: Duration,
) {
    fun expired(orderedAt: Instant, now: Instant): Boolean = !now.isBefore(orderedAt.plus(allowedFor))

    companion object {

        const val PERIOD_EXPIRED = "CANCEL_PERIOD_EXPIRED"
    }
}
