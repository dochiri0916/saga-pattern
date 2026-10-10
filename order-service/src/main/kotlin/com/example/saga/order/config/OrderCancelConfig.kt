package com.example.saga.order.config

import com.example.saga.order.application.saga.OrderCancelPolicy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

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
