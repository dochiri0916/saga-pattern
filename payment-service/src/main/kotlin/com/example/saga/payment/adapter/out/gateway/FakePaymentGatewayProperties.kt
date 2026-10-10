package com.example.saga.payment.adapter.out.gateway

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("payment.gateway")
data class FakePaymentGatewayProperties(

    val rejectOver: Int,

    val approveDelay: Duration = Duration.ZERO,
)
