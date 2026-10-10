package com.example.saga.platform.outbox

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.UUID

@AutoConfiguration(beforeName = ["org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"])
@ConditionalOnProperty(prefix = "saga.outbox", name = ["enabled"], havingValue = "true")
@AutoConfigurationPackage(basePackageClasses = [OutboxEntity::class])
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties::class)
@Import(OutboxAppender::class, OutboxShardLease::class, OutboxRelay::class, OutboxCleaner::class, OutboxMetrics::class)
class PlatformOutboxAutoConfiguration {

    @Bean
    fun outboxProducerFactoryCustomizer(
        @Value("\${spring.application.name}") applicationName: String,
    ): DefaultKafkaProducerFactoryCustomizer =
        DefaultKafkaProducerFactoryCustomizer { it.setTransactionIdPrefix("$applicationName-outbox-${UUID.randomUUID()}-") }
}
