package com.example.saga.infrastructure.messaging.kafka

import java.time.Duration
import java.util.Properties
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders

private val log = LoggerFactory.getLogger(DeadLetterRedriver::class.java)

class DeadLetterRedriver(
    private val consumerFactory: ConsumerFactory<String, String>,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    properties: ConsumerProperties,
) {

    private val topic = properties.deadLetter.name
    private val groupId = "$topic.redrive"

    fun pending(): Int = withConsumer { consumer, partitions ->
        val end = consumer.endOffsets(partitions)
        var pending = 0
        while (partitions.any { consumer.position(it) < end.getValue(it) }) {
            pending += consumer.poll(POLL_TIMEOUT).count { it.offset() < end.getValue(TopicPartition(it.topic(), it.partition())) }
        }
        pending
    }

    @Synchronized
    fun redrive(): Int = withConsumer { consumer, partitions ->
        val end = consumer.endOffsets(partitions)
        var redriven = 0
        while (partitions.any { consumer.position(it) < end.getValue(it) }) {
            val records = consumer.poll(POLL_TIMEOUT).filter { it.offset() < end.getValue(TopicPartition(it.topic(), it.partition())) }
            val outgoing = records.mapNotNull { it.toOriginal() }
            if (outgoing.isNotEmpty()) {
                kafkaTemplate.executeInTransaction { kafka -> outgoing.forEach { kafka.send(it) } }
            }

            partitions.forEach { if (consumer.position(it) > end.getValue(it)) consumer.seek(it, end.getValue(it)) }
            consumer.commitSync()
            redriven += outgoing.size
        }
        log.info("격리 토픽의 메시지를 원래 토픽으로 다시 보냈습니다. deadLetterTopic={}, redriven={}", topic, redriven)
        redriven
    }

    private fun ConsumerRecord<String, String>.toOriginal(): ProducerRecord<String, String>? {
        val originalTopic = headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC)?.value()?.toString(Charsets.UTF_8)
        if (originalTopic == null) {
            log.error("원래 토픽을 알 수 없어 다시 보내지 못했습니다. deadLetterTopic={}, partition={}, offset={}", topic(), partition(), offset())
            return null
        }
        return ProducerRecord(originalTopic, key(), value()).also { record ->

            headers().filterNot { it.key().startsWith(DLT_HEADER_PREFIX) }.forEach { record.headers().add(it) }
        }
    }

    private fun <T> withConsumer(block: (Consumer<String, String>, List<TopicPartition>) -> T): T {
        val overrides = Properties().apply { put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false") }
        return consumerFactory.createConsumer(groupId, "redrive", null, overrides).use { consumer ->
            val partitions = consumer.partitionsFor(topic).map { TopicPartition(it.topic(), it.partition()) }
            consumer.assign(partitions)
            val committed = consumer.committed(partitions.toSet())
            val beginning = consumer.beginningOffsets(partitions)
            partitions.forEach { consumer.seek(it, committed[it]?.offset() ?: beginning.getValue(it)) }
            block(consumer, partitions)
        }
    }

    private companion object {
        val POLL_TIMEOUT: Duration = Duration.ofSeconds(1)
        const val DLT_HEADER_PREFIX = "kafka_dlt-"
    }
}
