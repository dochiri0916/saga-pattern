package com.example.saga.order.adapter.out.config

import com.example.saga.order.domain.order.policy.OrderCancelPolicy
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("order.cancel")
data class OrderCancelProperties(
    val allowedFor: Duration,
)

@Configuration
@EnableConfigurationProperties(OrderCancelProperties::class)
class OrderCancelConfig {

    @Bean
    fun orderCancelPolicy(properties: OrderCancelProperties): OrderCancelPolicy =
        OrderCancelPolicy(properties.allowedFor)
}
