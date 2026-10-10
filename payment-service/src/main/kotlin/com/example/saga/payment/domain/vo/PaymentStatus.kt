package com.example.saga.payment.domain.vo

enum class PaymentStatus {

    PENDING,
    COMPLETED,

    REJECTED,
    CANCELLED;

    fun canTransitionTo(target: PaymentStatus): Boolean = when (this) {
        PENDING -> target != PENDING
        COMPLETED -> target == CANCELLED
        REJECTED, CANCELLED -> false
    }
}
