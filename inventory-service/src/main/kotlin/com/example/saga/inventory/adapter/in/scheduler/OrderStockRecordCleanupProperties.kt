package com.example.saga.inventory.adapter.`in`.scheduler

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("inventory.order-stock-records.cleanup")
data class OrderStockRecordCleanupProperties(
    val retention: Duration,

    val decreasedRetention: Duration,
    val batchSize: Int,
)
