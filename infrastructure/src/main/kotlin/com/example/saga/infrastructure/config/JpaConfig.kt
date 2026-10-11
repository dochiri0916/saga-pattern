package com.example.saga.infrastructure.config

import com.example.saga.infrastructure.audit.AuditJpaEntity
import com.example.saga.infrastructure.audit.AuditRecorder
import com.example.saga.infrastructure.idempotency.IdempotencyJpaEntity
import com.example.saga.infrastructure.messaging.inbox.InboxJpaEntity
import com.example.saga.infrastructure.messaging.outbox.OutboxJpaEntity
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@AutoConfiguration(beforeName = ["org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"])
@AutoConfigurationPackage(basePackageClasses = [AuditJpaEntity::class])
@Import(AuditRecorder::class, InboxJpaConfig::class, OutboxJpaConfig::class, IdempotencyJpaConfig::class)
class JpaConfig

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "saga.inbox", name = ["enabled"], havingValue = "true")
@AutoConfigurationPackage(basePackageClasses = [InboxJpaEntity::class])
class InboxJpaConfig

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "saga.outbox", name = ["enabled"], havingValue = "true")
@AutoConfigurationPackage(basePackageClasses = [OutboxJpaEntity::class])
class OutboxJpaConfig

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "saga.idempotency", name = ["enabled"], havingValue = "true")
@AutoConfigurationPackage(basePackageClasses = [IdempotencyJpaEntity::class])
class IdempotencyJpaConfig
