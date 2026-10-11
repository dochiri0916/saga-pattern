package com.example.saga.infrastructure.messaging.kafka

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("saga.consumer")
data class ConsumerProperties(
    val retry: Retry,
    val deadLetter: DeadLetter,
) {

    data class Retry(
        val initialInterval: Duration,
        val maxInterval: Duration,
    )

    data class DeadLetter(
        val name: String,
        val partitions: Int,
        val replicas: Int,
    )
}
