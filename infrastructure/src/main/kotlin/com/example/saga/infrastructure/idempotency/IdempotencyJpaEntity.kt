package com.example.saga.infrastructure.idempotency

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "http_idempotency", indexes = [Index(name = "idx_http_idempotency_created_at", columnList = "created_at")])
final class IdempotencyJpaEntity private constructor(
    @field:Id @field:Column(length = 64) val id: String,
    @field:Column(nullable = false, length = 128) val scope: String,
    @field:Column(name = "idempotency_key", nullable = false, length = 64) val key: String,
    @field:Column(name = "request_hash", nullable = false, length = 64) val requestHash: String,
    @field:Column(name = "resource_id", length = 128) val resourceId: String?,
    @field:Column(name = "created_at", nullable = false) val createdAt: Instant,
) {
    companion object {
        fun of(id: String, scope: String, key: String, requestHash: String, resourceId: String?, createdAt: Instant) =
            IdempotencyJpaEntity(id, scope, key, requestHash, resourceId, createdAt)
    }
}
