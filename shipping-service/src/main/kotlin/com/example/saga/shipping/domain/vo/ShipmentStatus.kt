package com.example.saga.shipping.domain.vo

enum class ShipmentStatus {
    REGISTERED,

    SHIPPED,
    CANCELLED;

    fun canTransitionTo(target: ShipmentStatus): Boolean = this == REGISTERED && target != REGISTERED
}
