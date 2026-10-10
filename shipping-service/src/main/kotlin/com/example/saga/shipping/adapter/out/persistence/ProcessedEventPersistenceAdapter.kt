package com.example.saga.shipping.adapter.out.persistence

import com.example.saga.shipping.application.port.out.ProcessedEventRepository
import com.example.saga.platform.idempotency.ProcessedEventStore
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProcessedEventPersistenceAdapter(
    private val processedEventStore: ProcessedEventStore,
) : ProcessedEventRepository {

    override fun saveIfAbsent(eventId: UUID): Boolean = processedEventStore.saveIfAbsent(eventId)
}
