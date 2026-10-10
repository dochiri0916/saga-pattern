package com.example.saga.order.application.port.out

import com.example.saga.order.domain.Order

interface InventoryClient {

    fun decreaseStock(order: Order): String?

    fun restoreStock(order: Order)
}
