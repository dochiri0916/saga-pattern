package com.example.saga.infrastructure.messaging.inbox

import java.time.Instant
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface InboxJpaRepository : JpaRepository<InboxJpaEntity, UUID> {

    @Modifying
    @Query(
        value = "insert ignore into processed_events (event_id, processed_at) values (:eventId, :processedAt)",
        nativeQuery = true,
    )
    fun insertIgnore(eventId: UUID, processedAt: Instant): Int

    @Transactional
    @Modifying
    @Query(value = "delete from processed_events where processed_at < :cutoff limit :batchSize", nativeQuery = true)
    fun deleteProcessedBefore(cutoff: Instant, batchSize: Int): Int
}
