package com.example.saga.infrastructure.messaging.kafka

import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@AutoConfiguration
@ConditionalOnProperty(prefix = "saga.outbox", name = ["enabled"], havingValue = "true")
@Import(KafkaMessagePublisher::class)
class KafkaProducerConfig {
    @Bean
    fun outboxProducerFactoryCustomizer(@Value("\${spring.application.name}") applicationName: String): DefaultKafkaProducerFactoryCustomizer =
        DefaultKafkaProducerFactoryCustomizer { it.setTransactionIdPrefix("$applicationName-outbox-${UUID.randomUUID()}-") }
}
