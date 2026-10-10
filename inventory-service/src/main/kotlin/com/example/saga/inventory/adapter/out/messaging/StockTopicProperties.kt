package com.example.saga.inventory.adapter.out.messaging

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("inventory.kafka.topics.stock-changed")
data class StockTopicProperties(
    val name: String,
    val partitions: Int,
    val replicas: Int,
)
