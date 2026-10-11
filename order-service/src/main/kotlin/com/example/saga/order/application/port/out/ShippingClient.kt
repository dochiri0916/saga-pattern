package com.example.saga.order.application.port.out

import com.example.saga.order.domain.order.Order

interface ShippingClient {

    fun cancelShipment(order: Order): String?
}
