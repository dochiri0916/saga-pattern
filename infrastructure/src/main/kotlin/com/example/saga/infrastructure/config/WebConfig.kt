package com.example.saga.infrastructure.config

import com.example.saga.infrastructure.web.ApiControllerAdvice
import com.example.saga.infrastructure.web.ProblemDetailFactory
import java.time.Clock
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@AutoConfiguration(after = [ClockConfig::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(ApiControllerAdvice::class)
class WebConfig {

    @Bean
    @ConditionalOnMissingBean
    fun problemDetailFactory(clock: Clock): ProblemDetailFactory = ProblemDetailFactory(clock)
}
