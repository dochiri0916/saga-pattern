package com.example.saga.order.adapter.`in`.scheduler

import com.example.saga.order.application.command.RecoverStalledOrderSagasCommand
import com.example.saga.order.application.port.`in`.RecoverStalledOrderSagasUseCase
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
@EnableConfigurationProperties(OrderSagaRecoveryProperties::class)
class OrderSagaRecoveryScheduler(
    private val recoverStalledOrderSagasUseCase: RecoverStalledOrderSagasUseCase,
    private val properties: OrderSagaRecoveryProperties,
) {

    @Scheduled(fixedDelayString = "\${order.saga.recovery.scan-interval}")
    fun recoverStalled() {
        val command = RecoverStalledOrderSagasCommand(properties.after, properties.batchSize)
        while (recoverStalledOrderSagasUseCase.recover(command) == properties.batchSize) {

        }
    }
}
