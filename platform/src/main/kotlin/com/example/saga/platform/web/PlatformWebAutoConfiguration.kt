package com.example.saga.platform.web

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.time.Clock

@AutoConfiguration(after = [PlatformClockAutoConfiguration::class])
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(ApiControllerAdvice::class)
class PlatformWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun problemDetailFactory(clock: Clock): ProblemDetailFactory = ProblemDetailFactory(clock)
}
