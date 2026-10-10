package com.example.saga.platform.outbox

import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Clock

@Transactional(propagation = Propagation.MANDATORY)
class OutboxAppender(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val outboxRelay: OutboxRelay,
    private val properties: OutboxProperties,
    private val clock: Clock,
) {

    fun append(eventType: String, topic: String, messageKey: String, payload: String) {
        outboxJpaRepository.save(
            OutboxEntity.of(
                eventId = UuidCreator.getTimeOrderedEpoch(),
                eventType = eventType,
                shard = properties.shardOf(messageKey),
                topic = topic,
                messageKey = messageKey,
                payload = payload,
                createdAt = clock.instant(),
            )
        )

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() = outboxRelay.wake()
        })
    }
}
