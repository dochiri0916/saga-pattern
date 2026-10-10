package com.example.saga.order.adapter.out.messaging

import com.example.saga.order.application.port.out.OrderEventPublisher
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.event.OrderCancelled
import com.example.saga.order.domain.event.OrderCompleted
import com.example.saga.order.domain.event.OrderItemCancelled
import com.example.saga.order.domain.event.OrderItemReturned
import com.example.saga.platform.outbox.OutboxAppender
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class OutboxOrderEventPublisher(
    private val outboxAppender: OutboxAppender,
    private val orderCompletedTopicProperties: OrderCompletedTopicProperties,
    private val jsonMapper: JsonMapper,
) : OrderEventPublisher {

    override fun publish(order: Order) = order.events.forEach { event ->
        when (event) {
            is OrderCompleted -> outboxAppender.append(
                OrderCompletedMessage.EVENT_TYPE,
                orderCompletedTopicProperties.name,
                order.id.toString(),
                jsonMapper.writeValueAsString(OrderCompletedMessage.from(order)),
            )

            is OrderCancelled, is OrderItemCancelled, is OrderItemReturned -> Unit
        }
    }
}
