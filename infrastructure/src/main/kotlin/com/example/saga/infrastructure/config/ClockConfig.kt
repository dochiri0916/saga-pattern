package com.example.saga.infrastructure.config

import java.time.Clock
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class ClockConfig {

    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock = Clock.systemUTC()
}
