package com.example.saga.infrastructure.idempotency

import java.time.Clock
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.Scheduled

@ConfigurationProperties("saga.idempotency.cleanup")
data class IdempotencyCleanupProperties(val retention: Duration = Duration.ofHours(24), val batchSize: Int = 1000)

class IdempotencyCleaner(private val repository: IdempotencyRepository, private val properties: IdempotencyCleanupProperties, private val clock: Clock) {
    @Scheduled(fixedDelayString = "\${saga.idempotency.cleanup.interval:10m}")
    fun clean() {
        val cutoff = clock.instant().minus(properties.retention)
        do { val deleted = repository.deleteCreatedBefore(cutoff, properties.batchSize) } while (deleted == properties.batchSize)
    }
}
