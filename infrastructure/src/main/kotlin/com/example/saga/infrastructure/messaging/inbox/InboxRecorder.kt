package com.example.saga.infrastructure.messaging.inbox

import java.time.Clock
import java.util.UUID
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(propagation = Propagation.MANDATORY)
class InboxRecorder(
    private val inboxJpaRepository: InboxJpaRepository,
    private val clock: Clock,
) {

    fun recordIfAbsent(eventId: UUID): Boolean = inboxJpaRepository.insertIgnore(eventId, clock.instant()) == 1
}
