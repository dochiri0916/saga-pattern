package com.example.saga.inventory.application.saga

import com.example.saga.inventory.domain.vo.OrderId

data class OrderStockRecord(
    val orderId: OrderId,
    val status: OrderStockStatus,
)

enum class OrderStockStatus {

    DECREASED,

    REJECTED,

    RESTORED,

    CANCELLED,
}

enum class StockDecreaseRejectionReason {
    STOCK_NOT_FOUND,
    INSUFFICIENT_STOCK,
    INVALID_ORDER_ITEM,

    PREVIOUSLY_REJECTED,

    ORDER_CANCELLED,
}
