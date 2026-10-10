package com.example.saga.platform.consumer

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.TopicBuilder
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.ExponentialBackOff
import tools.jackson.databind.json.JsonMapper

private val log = LoggerFactory.getLogger(PlatformConsumerAutoConfiguration::class.java)

@AutoConfiguration
@ConditionalOnProperty(prefix = "saga.consumer", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(ConsumerProperties::class)
class PlatformConsumerAutoConfiguration {

    @Bean
    fun messageReader(jsonMapper: JsonMapper): MessageReader = MessageReader(jsonMapper)

    @Bean
    fun consumerDeadLetterTopic(properties: ConsumerProperties): NewTopic =
        TopicBuilder.name(properties.deadLetter.name)
            .partitions(properties.deadLetter.partitions)
            .replicas(properties.deadLetter.replicas)
            .build()

    @Bean
    fun deadLetterRedriver(
        consumerFactory: ConsumerFactory<String, String>,
        kafkaTemplate: KafkaTemplate<String, String>,
        properties: ConsumerProperties,
    ): DeadLetterRedriver = DeadLetterRedriver(consumerFactory, kafkaTemplate, properties)

    @Bean
    fun deadLetterEndpoint(deadLetterRedriver: DeadLetterRedriver): DeadLetterEndpoint = DeadLetterEndpoint(deadLetterRedriver)

    @Bean
    fun consumerErrorHandler(
        kafkaTemplate: KafkaTemplate<*, *>,
        properties: ConsumerProperties,
        meterRegistry: MeterRegistry,
    ): DefaultErrorHandler {

        val deadLettered = Counter.builder("saga.consumer.dead-lettered")
            .description("처리할 수 없어 격리 토픽으로 옮긴 메시지 수")
            .register(meterRegistry)

        val deadLetterPublisher =
            DeadLetterPublishingRecoverer(kafkaTemplate) { _, _ -> TopicPartition(properties.deadLetter.name, -1) }
        val recoverer = ConsumerRecordRecoverer { record, exception ->
            log.error(
                "처리할 수 없는 메시지를 격리 토픽으로 옮깁니다. topic={}, partition={}, offset={}, deadLetterTopic={}, cause={}",
                record.topic(), record.partition(), record.offset(), properties.deadLetter.name, exception.cause?.message
            )
            deadLetterPublisher.accept(record, exception)
            deadLettered.increment()
        }
        val backOff = ExponentialBackOff(properties.retry.initialInterval.toMillis(), BACKOFF_MULTIPLIER).apply {
            maxInterval = properties.retry.maxInterval.toMillis()
        }
        return DefaultErrorHandler(recoverer, backOff).apply {
            addNotRetryableExceptions(UnprocessableMessageException::class.java)
        }
    }

    private companion object {
        const val BACKOFF_MULTIPLIER = 2.0
    }
}
