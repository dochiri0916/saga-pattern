package com.example.saga.order.adapter.`in`.scheduling

import com.example.saga.order.application.port.`in`.command.CleanOrderIdempotencyKeysCommand
import com.example.saga.order.application.port.`in`.usecase.CleanOrderIdempotencyKeysUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(OrderIdempotencyKeyCleanupScheduler::class.java)

@Component
@EnableScheduling
@EnableConfigurationProperties(OrderIdempotencyKeyCleanupProperties::class)
class OrderIdempotencyKeyCleanupScheduler(
    private val cleanOrderIdempotencyKeysUseCase: CleanOrderIdempotencyKeysUseCase,
    private val properties: OrderIdempotencyKeyCleanupProperties,
) {

    @Scheduled(fixedDelayString = "\${order.idempotency-keys.cleanup.interval}")
    fun clean() {
        val command = CleanOrderIdempotencyKeysCommand(properties.retention, properties.batchSize)
        var deleted = 0
        do {
            val batch = cleanOrderIdempotencyKeysUseCase.clean(command)
            deleted += batch
        } while (batch == properties.batchSize)
        if (deleted > 0) log.info("보관 기간이 지난 멱등 키를 삭제했습니다. deleted={}", deleted)
    }
}
