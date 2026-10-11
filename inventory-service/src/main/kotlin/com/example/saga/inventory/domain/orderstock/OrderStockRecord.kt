package com.example.saga.inventory.domain.orderstock

import com.example.saga.inventory.domain.orderstock.vo.OrderReferenceId
import com.example.saga.inventory.domain.orderstock.vo.OrderStockStatus

data class OrderStockRecord(
    val orderId: OrderReferenceId,
    val status: OrderStockStatus,
)
