package com.example.saga.infrastructure.messaging.inbox

import java.util.UUID
import org.springframework.transaction.annotation.Transactional

@Transactional
class InboxGuard(private val inboxRecorder: InboxRecorder) {
    fun execute(eventId: UUID, handler: () -> Unit): Boolean {
        if (!inboxRecorder.recordIfAbsent(eventId)) return false
        handler()
        return true
    }
}
