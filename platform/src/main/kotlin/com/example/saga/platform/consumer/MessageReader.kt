package com.example.saga.platform.consumer

import com.example.saga.platform.outbox.OutboxHeaders
import org.apache.kafka.clients.consumer.ConsumerRecord
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

class MessageReader(private val jsonMapper: JsonMapper) {

    fun eventType(record: ConsumerRecord<String, String>): String = header(record, OutboxHeaders.EVENT_TYPE)

    fun eventId(record: ConsumerRecord<String, String>): UUID =
        try {
            UUID.fromString(header(record, OutboxHeaders.EVENT_ID))
        } catch (e: IllegalArgumentException) {
            throw UnprocessableMessageException("${OutboxHeaders.EVENT_ID} 헤더가 UUID가 아닙니다. ${position(record)}", e)
        }

    fun <T : Any> body(record: ConsumerRecord<String, String>, type: Class<T>): T {
        val body: T? = try {
            record.value()?.let { jsonMapper.readValue(it, type) }
        } catch (e: JacksonException) {
            throw UnprocessableMessageException("본문을 ${type.simpleName}(으)로 해석할 수 없습니다. ${position(record)}", e)
        }
        return body ?: throw UnprocessableMessageException("본문이 비어 있습니다. ${position(record)}")
    }

    fun position(record: ConsumerRecord<String, String>): String =
        "topic=${record.topic()}, partition=${record.partition()}, offset=${record.offset()}"

    private fun header(record: ConsumerRecord<String, String>, name: String): String =
        record.headers().lastHeader(name)?.value()?.toString(Charsets.UTF_8)
            ?: throw UnprocessableMessageException("$name 헤더가 없습니다. ${position(record)}")
}
