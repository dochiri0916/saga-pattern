package com.example.saga.payment.adapter.`in`.scheduling

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("payment.order-payment-cancellations.cleanup")
data class OrderPaymentCancellationCleanupProperties(
    val retention: Duration,
    val batchSize: Int,
)
