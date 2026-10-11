package com.example.saga.infrastructure.messaging.outbox

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import java.time.Clock
import java.time.Duration

class OutboxMetrics(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val properties: OutboxProperties,
    private val clock: Clock,
    meterRegistry: MeterRegistry,
) {

    private val shards = (0 until properties.shardCount).toList()

    init {
        Gauge.builder("saga.outbox.pending.count") { outboxJpaRepository.countByShardInAndStatus(shards, OutboxStatus.PENDING).toDouble() }
            .description("아직 발행되지 않은 아웃박스 이벤트 수")
            .register(meterRegistry)
        Gauge.builder("saga.outbox.pending.oldest.age") { oldestPendingAge().toMillis() / 1000.0 }
            .description("가장 오래 기다린 미발행 이벤트의 대기 시간")
            .baseUnit("seconds")
            .register(meterRegistry)
    }

    private fun oldestPendingAge(): Duration {
        val oldestCreatedAt = shards
            .mapNotNull { outboxJpaRepository.findFirstByShardAndStatusOrderByIdAsc(it, OutboxStatus.PENDING)?.createdAt }
            .minOrNull()
            ?: return Duration.ZERO
        return Duration.between(oldestCreatedAt, clock.instant()).coerceAtLeast(Duration.ZERO)
    }
}
