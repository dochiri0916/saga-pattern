package com.example.saga.order.application.service

import com.example.saga.order.application.port.`in`.command.RecoverStalledOrderSagasCommand
import com.example.saga.order.application.port.`in`.usecase.RecoverStalledOrderSagasUseCase
import com.example.saga.order.application.port.out.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock

private val log = LoggerFactory.getLogger(RecoverStalledOrderSagasService::class.java)

@Service
class RecoverStalledOrderSagasService(
    private val orderRepository: OrderRepository,
    private val orderSagaStepExecutor: OrderSagaStepExecutor,
    private val orderSagaDriver: OrderSagaDriver,
    private val clock: Clock,
) : RecoverStalledOrderSagasUseCase {

    override fun recover(command: RecoverStalledOrderSagasCommand): Int {
        val sagas = orderSagaStepExecutor.claimStalled(clock.instant().minus(command.stalledFor), command.limit)
        for (saga in sagas) {
            try {
                val order = orderRepository.findById(saga.orderId)
                if (order == null) {
                    log.error("멈춘 사가의 주문이 없어 복구하지 못했습니다. orderId={}", saga.orderId)
                    continue
                }
                val result = orderSagaDriver.drive(order, saga)
                log.warn("멈춘 사가를 복구했습니다. orderId={}, {} -> {}", saga.orderId, saga.status, result.status)
            } catch (e: RuntimeException) {

                log.error("멈춘 사가를 복구하다 실패했습니다. orderId={}", saga.orderId, e)
            }
        }
        return sagas.size
    }
}
