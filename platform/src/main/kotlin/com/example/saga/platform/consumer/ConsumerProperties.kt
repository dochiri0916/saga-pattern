package com.example.saga.platform.consumer

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

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
