package com.example.saga.order.domain.order.vo

enum class OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED;

    fun canTransitionTo(target: OrderStatus): Boolean = when (this) {
        PENDING -> target == COMPLETED || target == CANCELLED
        COMPLETED -> target == CANCELLED
        CANCELLED -> false
    }
}
