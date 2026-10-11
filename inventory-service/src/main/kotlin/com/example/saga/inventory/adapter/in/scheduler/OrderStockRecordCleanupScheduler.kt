package com.example.saga.inventory.adapter.`in`.scheduler

import com.example.saga.inventory.application.command.CleanOrderStockRecordsCommand
import com.example.saga.inventory.application.port.`in`.CleanOrderStockRecordsUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(OrderStockRecordCleanupScheduler::class.java)

@Component
@EnableScheduling
@EnableConfigurationProperties(OrderStockRecordCleanupProperties::class)
class OrderStockRecordCleanupScheduler(
    private val cleanOrderStockRecordsUseCase: CleanOrderStockRecordsUseCase,
    private val properties: OrderStockRecordCleanupProperties,
) {

    @Scheduled(fixedDelayString = "\${inventory.order-stock-records.cleanup.interval}")
    fun clean() {
        val command = CleanOrderStockRecordsCommand(properties.retention, properties.decreasedRetention, properties.batchSize)
        var deleted = 0
        do {
            val batch = cleanOrderStockRecordsUseCase.clean(command)
            deleted += batch
        } while (batch == properties.batchSize)
        if (deleted > 0) log.info("보관 기간이 지난 주문별 재고 기록을 삭제했습니다. deleted={}", deleted)
    }
}
