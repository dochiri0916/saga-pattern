package com.example.saga.inventory.adapter.out.messaging

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
@EnableConfigurationProperties(StockTopicProperties::class)
class StockMessagingConfig {

    @Bean
    fun stockChangedTopic(properties: StockTopicProperties): NewTopic = TopicBuilder.name(properties.name)
        .partitions(properties.partitions)
        .replicas(properties.replicas)
        .build()
}
