package com.example.saga.order.adapter.`in`.monitoring

import com.example.saga.order.application.port.out.OrderSagaRepository
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration

@Component
class OrderSagaMetrics(
    private val orderSagaRepository: OrderSagaRepository,
    private val clock: Clock,
    @Value("\${order.saga.stalled-after}") private val stalledAfter: Duration,
    meterRegistry: MeterRegistry,
) {

    init {
        Gauge.builder("order.saga.stalled.count") {
            orderSagaRepository.countUnfinishedStartedBefore(clock.instant().minus(stalledAfter)).toDouble()
        }
            .description("시작한 지 기준 시간이 넘었는데 끝나지 않은 사가 수")
            .register(meterRegistry)
    }
}
