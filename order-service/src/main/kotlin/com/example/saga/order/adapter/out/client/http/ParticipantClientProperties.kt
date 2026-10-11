package com.example.saga.order.adapter.out.client.http

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("order.clients")
data class ParticipantClientProperties(
    val inventory: Client,
    val payment: Client,
    val shipping: Client,
    val circuitBreaker: CircuitBreaker,
) {

    data class CircuitBreaker(
        val failureRateThreshold: Float,
        val slidingWindowSize: Int,

        val minimumNumberOfCalls: Int,
        val openDuration: Duration,
        val halfOpenCalls: Int,
    )

    data class Client(

        val baseUrls: List<String>,
        val connectTimeout: Duration,

        val readTimeout: Duration,

        val ejectDuration: Duration,
    )
}
