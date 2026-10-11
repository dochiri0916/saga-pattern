package com.example.saga.infrastructure.messaging.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "outbox_shards")
final class OutboxShardEntity private constructor(
    shard: Int,
) {

    @field:Id
    @field:Column(name = "shard", nullable = false, updatable = false)
    final var shard: Int = shard
        private set
}
