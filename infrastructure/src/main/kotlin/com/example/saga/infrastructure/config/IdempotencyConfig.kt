package com.example.saga.infrastructure.config

import com.example.saga.infrastructure.idempotency.IdempotencyCleaner
import com.example.saga.infrastructure.idempotency.IdempotencyCleanupProperties
import com.example.saga.infrastructure.idempotency.IdempotencyInterceptor
import com.example.saga.infrastructure.idempotency.IdempotencyManager
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration(beforeName = ["org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"])
@ConditionalOnProperty(prefix = "saga.idempotency", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(IdempotencyCleanupProperties::class)
@EnableScheduling
@Import(IdempotencyManager::class, IdempotencyInterceptor::class, IdempotencyCleaner::class)
class IdempotencyConfig
