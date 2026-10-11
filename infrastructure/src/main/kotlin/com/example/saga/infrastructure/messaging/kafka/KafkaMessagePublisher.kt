package com.example.saga.infrastructure.messaging.kafka

import com.example.saga.infrastructure.messaging.outbox.OutboxHeaders
import java.util.UUID
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate

data class KafkaMessage(val eventId: UUID, val eventType: String, val topic: String, val key: String, val payload: String)

class KafkaMessagePublisher(private val kafkaTemplate: KafkaTemplate<String, String>) {
    fun publish(messages: List<KafkaMessage>) {
        kafkaTemplate.executeInTransaction { kafka ->
            messages.forEach { message ->
                kafka.send(ProducerRecord(message.topic, message.key, message.payload).apply {
                    headers().add(OutboxHeaders.EVENT_ID, message.eventId.toString().toByteArray())
                    headers().add(OutboxHeaders.EVENT_TYPE, message.eventType.toByteArray())
                })
            }
        }
    }
}
