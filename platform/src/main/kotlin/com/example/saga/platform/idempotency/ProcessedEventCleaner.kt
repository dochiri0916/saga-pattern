package com.example.saga.platform.idempotency

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Clock

private val log = LoggerFactory.getLogger(ProcessedEventCleaner::class.java)

class ProcessedEventCleaner(
    private val processedEventJpaRepository: ProcessedEventJpaRepository,
    private val properties: ProcessedEventCleanupProperties,
    private val clock: Clock,
) {

    @Scheduled(fixedDelayString = "\${saga.processed-events.cleanup.interval}")
    fun clean() {
        val cutoff = clock.instant().minus(properties.retention)
        var deleted = 0

        do {
            val batch = processedEventJpaRepository.deleteProcessedBefore(cutoff, properties.batchSize)
            deleted += batch
        } while (batch == properties.batchSize)
        if (deleted > 0) log.info("보관 기간이 지난 이벤트 처리 기록을 삭제했습니다. deleted={}, cutoff={}", deleted, cutoff)
    }
}
