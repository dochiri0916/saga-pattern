package com.example.saga.infrastructure.config

import com.example.saga.infrastructure.messaging.serialization.EnvelopeDeserializer
import com.example.saga.infrastructure.messaging.serialization.EventSerializer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import tools.jackson.databind.json.JsonMapper

@AutoConfiguration
class JacksonConfig {
    @Bean
    fun envelopeDeserializer(): EnvelopeDeserializer = EnvelopeDeserializer()

    @Bean
    fun eventSerializer(jsonMapper: JsonMapper): EventSerializer = EventSerializer(jsonMapper)
}
