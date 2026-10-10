package com.example.saga.platform.idempotency

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Transactional(propagation = Propagation.MANDATORY)
class ProcessedEventStore(
    private val processedEventJpaRepository: ProcessedEventJpaRepository,
    private val clock: Clock,
) {

    fun saveIfAbsent(eventId: UUID): Boolean = processedEventJpaRepository.insertIgnore(eventId, clock.instant()) == 1
}
