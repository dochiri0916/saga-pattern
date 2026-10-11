package com.example.saga.infrastructure.messaging.serialization

import com.example.saga.infrastructure.messaging.outbox.OutboxHeaders
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.apache.kafka.clients.consumer.ConsumerRecord

class EnvelopeDeserializerTest {
    private val deserializer = EnvelopeDeserializer()

    private fun record(payload: String): ConsumerRecord<String, String> = ConsumerRecord("events", 0, 4L, "key", payload)

    @Test
    fun `envelope만 해석하고 payload는 feature mapper에 그대로 전달한다`() {
        val eventId = UUID.randomUUID()
        val record = record("feature 전용 payload")
        record.headers().add(OutboxHeaders.EVENT_ID, eventId.toString().toByteArray())
        record.headers().add(OutboxHeaders.EVENT_TYPE, "EXTERNAL_EVENT".toByteArray())
        val envelope = deserializer.deserialize(record)
        assertEquals(eventId, envelope.eventId)
        assertEquals("EXTERNAL_EVENT", envelope.eventType)
        assertEquals(record.value(), envelope.payload)
    }

    @Test
    fun `누락된 헤더와 잘못된 UUID 및 빈 본문은 처리 불가 메시지로 분류한다`() {
        assertFailsWith<UnprocessableMessageException> { deserializer.deserialize(record("{}")) }
        val invalidId = record("{}")
        invalidId.headers().add(OutboxHeaders.EVENT_ID, "invalid".toByteArray())
        invalidId.headers().add(OutboxHeaders.EVENT_TYPE, "EVENT".toByteArray())
        assertFailsWith<UnprocessableMessageException> { deserializer.deserialize(invalidId) }
        val empty = record("")
        empty.headers().add(OutboxHeaders.EVENT_ID, UUID.randomUUID().toString().toByteArray())
        empty.headers().add(OutboxHeaders.EVENT_TYPE, "EVENT".toByteArray())
        assertFailsWith<UnprocessableMessageException> { deserializer.deserialize(empty) }
    }
}
