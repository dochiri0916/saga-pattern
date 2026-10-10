package com.example.saga.platform.idempotency

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration(beforeName = ["org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"])
@ConditionalOnProperty(prefix = "saga.processed-events", name = ["enabled"], havingValue = "true")
@AutoConfigurationPackage(basePackageClasses = [ProcessedEventEntity::class])
@EnableScheduling
@EnableConfigurationProperties(ProcessedEventCleanupProperties::class)
@Import(ProcessedEventStore::class, ProcessedEventCleaner::class)
class PlatformProcessedEventAutoConfiguration
