package com.example.saga.infrastructure.messaging.inbox

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration(beforeName = ["org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"])
@ConditionalOnProperty(prefix = "saga.inbox", name = ["enabled"], havingValue = "true")
@EnableScheduling
@EnableConfigurationProperties(InboxCleanupProperties::class)
@Import(InboxRecorder::class, InboxGuard::class, InboxCleaner::class)
class InboxConfig
