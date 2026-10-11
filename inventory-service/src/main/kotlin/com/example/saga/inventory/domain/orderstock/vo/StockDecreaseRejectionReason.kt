package com.example.saga.inventory.domain.orderstock.vo

enum class StockDecreaseRejectionReason {
    STOCK_NOT_FOUND,
    INSUFFICIENT_STOCK,
    INVALID_ORDER_ITEM,

    PREVIOUSLY_REJECTED,

    ORDER_CANCELLED,
}
