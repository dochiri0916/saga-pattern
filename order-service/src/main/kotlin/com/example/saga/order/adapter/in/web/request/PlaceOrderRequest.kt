package com.example.saga.order.adapter.`in`.web.request

import java.util.UUID

data class PlaceOrderRequest(val items: List<PlaceOrderItemRequest>)
data class PlaceOrderItemRequest(val stockId: UUID, val quantity: Int)
