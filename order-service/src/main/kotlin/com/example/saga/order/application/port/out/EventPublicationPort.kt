package com.example.saga.order.application.port.out

import com.example.saga.order.contract.event.OrderCompletedEvent

interface EventPublicationPort { fun publish(event: OrderCompletedEvent) }
