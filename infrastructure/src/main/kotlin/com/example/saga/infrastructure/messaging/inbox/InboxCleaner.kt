package com.example.saga.infrastructure.messaging.inbox

import java.time.Clock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

private val log = LoggerFactory.getLogger(InboxCleaner::class.java)

class InboxCleaner(
    private val inboxJpaRepository: InboxJpaRepository,
    private val properties: InboxCleanupProperties,
    private val clock: Clock,
) {

    @Scheduled(fixedDelayString = "\${saga.inbox.cleanup.interval}")
    fun clean() {
        val cutoff = clock.instant().minus(properties.retention)
        var deleted = 0

        do {
            val batch = inboxJpaRepository.deleteProcessedBefore(cutoff, properties.batchSize)
            deleted += batch
        } while (batch == properties.batchSize)
        if (deleted > 0) log.info("보관 기간이 지난 이벤트 처리 기록을 삭제했습니다. deleted={}, cutoff={}", deleted, cutoff)
    }
}
