package com.example.saga.infrastructure.messaging.outbox

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration(beforeName = ["org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"])
@ConditionalOnProperty(prefix = "saga.outbox", name = ["enabled"], havingValue = "true")
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties::class)
@Import(OutboxWriter::class, OutboxShardLease::class, OutboxRelay::class, OutboxCleaner::class, OutboxMetrics::class, OutboxRetryPolicy::class)
class OutboxConfig
