package com.example.saga.infrastructure.messaging.serialization

import com.example.saga.infrastructure.messaging.outbox.OutboxHeaders
import java.util.UUID
import org.apache.kafka.clients.consumer.ConsumerRecord

data class EventEnvelope(val eventId: UUID, val eventType: String, val payload: String, val position: String)

class EnvelopeDeserializer {
    fun deserialize(record: ConsumerRecord<String, String>): EventEnvelope {
        val position = "topic=${record.topic()}, partition=${record.partition()}, offset=${record.offset()}"
        fun header(name: String): String = record.headers().lastHeader(name)?.value()?.toString(Charsets.UTF_8)
            ?: throw UnprocessableMessageException("$name 헤더가 없습니다. $position")
        val eventId = try { UUID.fromString(header(OutboxHeaders.EVENT_ID)) } catch (e: IllegalArgumentException) {
            throw UnprocessableMessageException("eventId 헤더가 UUID가 아닙니다. $position", e)
        }
        val payload = record.value()?.takeIf { it.isNotBlank() }
            ?: throw UnprocessableMessageException("본문이 비어 있습니다. $position")
        return EventEnvelope(eventId, header(OutboxHeaders.EVENT_TYPE), payload, position)
    }
}
