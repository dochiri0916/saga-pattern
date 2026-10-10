package com.example.saga.inventory.adapter.`in`.scheduling

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("inventory.order-stock-records.cleanup")
data class OrderStockRecordCleanupProperties(
    val retention: Duration,

    val decreasedRetention: Duration,
    val batchSize: Int,
)
