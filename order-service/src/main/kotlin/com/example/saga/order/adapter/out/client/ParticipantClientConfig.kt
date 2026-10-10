package com.example.saga.order.adapter.out.client

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Clock

private val log = LoggerFactory.getLogger(ParticipantClientConfig::class.java)

@Configuration
@EnableConfigurationProperties(ParticipantClientProperties::class)
class ParticipantClientConfig {

    @Bean
    fun inventoryInstances(properties: ParticipantClientProperties, clock: Clock): ParticipantInstances =
        instances("inventory", properties.inventory, clock)

    @Bean
    fun paymentInstances(properties: ParticipantClientProperties, clock: Clock): ParticipantInstances =
        instances("payment", properties.payment, clock)

    @Bean
    fun shippingInstances(properties: ParticipantClientProperties, clock: Clock): ParticipantInstances =
        instances("shipping", properties.shipping, clock)

    @Bean
    fun participantCircuitBreakerRegistry(properties: ParticipantClientProperties): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(properties.circuitBreaker.slidingWindowSize)
            .minimumNumberOfCalls(properties.circuitBreaker.minimumNumberOfCalls)
            .failureRateThreshold(properties.circuitBreaker.failureRateThreshold)
            .waitDurationInOpenState(properties.circuitBreaker.openDuration)
            .permittedNumberOfCallsInHalfOpenState(properties.circuitBreaker.halfOpenCalls)
            .build()
        return CircuitBreakerRegistry.of(config).apply {
            eventPublisher.onEntryAdded { added ->
                added.addedEntry.eventPublisher.onStateTransition {
                    log.warn("회로 상태가 바뀌었습니다. participant={}, {}", it.circuitBreakerName, it.stateTransition)
                }
            }
        }
    }

    private fun instances(name: String, client: ParticipantClientProperties.Client, clock: Clock): ParticipantInstances {
        val httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(client.connectTimeout)
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply { setReadTimeout(client.readTimeout) }
        val instances = client.baseUrls.map { baseUrl ->
            ParticipantInstances.Instance(baseUrl, RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build())
        }
        return ParticipantInstances(name, instances, client.ejectDuration, clock)
    }
}
