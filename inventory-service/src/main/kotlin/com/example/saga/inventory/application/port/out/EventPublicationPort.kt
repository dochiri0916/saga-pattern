package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.contract.event.StockChangedEvent

interface EventPublicationPort { fun publish(event: StockChangedEvent) }
