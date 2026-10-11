package com.example.saga.order.domain.order.vo

enum class OrderItemStatus {
    ORDERED,
    CANCELLED,
    RETURNED;

    fun canTransitionTo(target: OrderItemStatus): Boolean = when (this) {
        ORDERED -> target == CANCELLED || target == RETURNED
        CANCELLED, RETURNED -> false
    }
}
