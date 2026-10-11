package com.example.saga.infrastructure.messaging.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(
    name = "outbox",
    indexes = [Index(name = "idx_outbox_shard_status_id", columnList = "shard, status, id")],
)
final class OutboxJpaEntity private constructor(
    eventId: UUID,
    eventType: String,
    shard: Int,
    topic: String,
    messageKey: String,
    payload: String,
    createdAt: Instant,
) {

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    final var id: Long? = null
        private set

    @field:Column(name = "event_id", nullable = false, updatable = false, unique = true)
    final var eventId: UUID = eventId
        private set

    @field:Column(name = "event_type", nullable = false, updatable = false)
    final var eventType: String = eventType
        private set

    @field:Column(name = "shard", nullable = false, updatable = false)
    final var shard: Int = shard
        private set

    @field:Column(name = "topic", nullable = false, updatable = false)
    final var topic: String = topic
        private set

    @field:Column(name = "message_key", nullable = false, updatable = false)
    final var messageKey: String = messageKey
        private set

    @field:Column(name = "payload", nullable = false, updatable = false, columnDefinition = "text")
    final var payload: String = payload
        private set

    @field:Column(name = "created_at", nullable = false, updatable = false)
    final var createdAt: Instant = createdAt
        private set

    @field:Enumerated(EnumType.STRING)
    @field:JdbcTypeCode(SqlTypes.VARCHAR)
    @field:Column(name = "status", nullable = false)
    final var status: OutboxStatus = OutboxStatus.PENDING
        private set

    @field:Column(name = "published_at")
    final var publishedAt: Instant? = null
        private set

    companion object {
        fun of(
            eventId: UUID,
            eventType: String,
            shard: Int,
            topic: String,
            messageKey: String,
            payload: String,
            createdAt: Instant,
        ): OutboxJpaEntity = OutboxJpaEntity(eventId, eventType, shard, topic, messageKey, payload, createdAt)
    }
}
