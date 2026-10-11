package com.example.saga.payment.adapter.`in`.scheduler

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("payment.order-payment-cancellations.cleanup")
data class OrderPaymentCancellationCleanupProperties(
    val retention: Duration,
    val batchSize: Int,
)
