package com.example.saga.platform.idempotency

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "processed_events",
    indexes = [Index(name = "idx_processed_events_processed_at", columnList = "processed_at")],
)
final class ProcessedEventEntity private constructor(
    eventId: UUID,
    processedAt: Instant,
) {

    @field:Id
    @field:Column(name = "event_id", nullable = false, updatable = false)
    final var eventId: UUID = eventId
        private set

    @field:Column(name = "processed_at", nullable = false, updatable = false)
    final var processedAt: Instant = processedAt
        private set
}
