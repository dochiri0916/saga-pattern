package com.example.saga.infrastructure.messaging.outbox

import com.github.f4b6a3.uuid.UuidCreator
import java.time.Clock
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Transactional(propagation = Propagation.MANDATORY)
class OutboxWriter(
    private val outboxJpaRepository: OutboxJpaRepository,
    private val outboxRelay: OutboxRelay,
    private val properties: OutboxProperties,
    private val clock: Clock,
) {

    fun write(eventType: String, topic: String, messageKey: String, payload: String) {
        outboxJpaRepository.save(
            OutboxJpaEntity.of(
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
