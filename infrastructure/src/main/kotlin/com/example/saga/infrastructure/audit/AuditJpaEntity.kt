package com.example.saga.infrastructure.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "audit_trail", indexes = [Index(name = "idx_audit_aggregate", columnList = "context, aggregate_id, recorded_at")])
final class AuditJpaEntity private constructor(
    @field:Column(nullable = false, updatable = false) val context: String,
    @field:Column(name = "aggregate_id", nullable = false, updatable = false) val aggregateId: String,
    @field:Column(nullable = false, updatable = false) val action: String,
    @field:Column(updatable = false, columnDefinition = "text") val detail: String?,
    @field:Column(name = "recorded_at", nullable = false, updatable = false) val recordedAt: Instant,
) {
    @field:Id @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

    companion object {
        fun of(context: String, aggregateId: String, action: String, detail: String?, recordedAt: Instant) =
            AuditJpaEntity(context, aggregateId, action, detail, recordedAt)
    }
}
