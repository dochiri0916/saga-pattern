package com.example.saga.infrastructure.messaging.outbox

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("saga.outbox")
data class OutboxProperties(
    val shardCount: Int,
    val relay: Relay,
    val cleanup: Cleanup,
) {

    fun shardOf(messageKey: String): Int = Math.floorMod(messageKey.hashCode(), shardCount)

    data class Relay(

        val concurrency: Int,
        val pollInterval: Duration,
        val batchSize: Int,

        val leaseDuration: Duration,
    )

    data class Cleanup(
        val retention: Duration,
        val batchSize: Int,
    )
}
