package com.example.saga.order.application.command

import java.util.UUID

data class PlaceOrderCommand(val items: List<PlaceOrderItemCommand>)
data class PlaceOrderItemCommand(val stockId: UUID, val quantity: Int)
