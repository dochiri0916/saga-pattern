package com.example.saga.shipping.domain.shipment.vo

enum class ShipmentStatus {
    REGISTERED,

    SHIPPED,
    CANCELLED;

    fun canTransitionTo(target: ShipmentStatus): Boolean = this == REGISTERED && target != REGISTERED
}
