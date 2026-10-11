package com.example.saga.infrastructure.messaging.serialization

import tools.jackson.databind.json.JsonMapper

class EventSerializer(private val jsonMapper: JsonMapper) {
    fun serialize(event: Any): String = jsonMapper.writeValueAsString(event)
}
