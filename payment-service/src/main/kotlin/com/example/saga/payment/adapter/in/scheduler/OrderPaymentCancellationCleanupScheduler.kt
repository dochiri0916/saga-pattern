package com.example.saga.payment.adapter.`in`.scheduler

import com.example.saga.payment.application.command.CleanOrderPaymentCancellationsCommand
import com.example.saga.payment.application.port.`in`.CleanOrderPaymentCancellationsUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(OrderPaymentCancellationCleanupScheduler::class.java)

@Component
@EnableScheduling
@EnableConfigurationProperties(OrderPaymentCancellationCleanupProperties::class)
class OrderPaymentCancellationCleanupScheduler(
    private val cleanOrderPaymentCancellationsUseCase: CleanOrderPaymentCancellationsUseCase,
    private val properties: OrderPaymentCancellationCleanupProperties,
) {

    @Scheduled(fixedDelayString = "\${payment.order-payment-cancellations.cleanup.interval}")
    fun clean() {
        val command = CleanOrderPaymentCancellationsCommand(properties.retention, properties.batchSize)
        var deleted = 0
        do {
            val batch = cleanOrderPaymentCancellationsUseCase.clean(command)
            deleted += batch
        } while (batch == properties.batchSize)
        if (deleted > 0) log.info("보관 기간이 지난 주문별 결제 취소 기록을 삭제했습니다. deleted={}", deleted)
    }
}
