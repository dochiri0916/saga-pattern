package com.example.saga.shipping.application.port.out

import java.util.UUID

interface ProcessedEventRepository {

    fun saveIfAbsent(eventId: UUID): Boolean
}
