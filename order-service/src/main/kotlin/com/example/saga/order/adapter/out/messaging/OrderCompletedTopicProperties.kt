package com.example.saga.order.adapter.out.messaging

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("order.kafka.topics.order-completed")
data class OrderCompletedTopicProperties(
    val name: String,
    val partitions: Int,
    val replicas: Int,
)
