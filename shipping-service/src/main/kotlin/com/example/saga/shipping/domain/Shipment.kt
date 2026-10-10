package com.example.saga.shipping.domain

import com.example.saga.shipping.domain.exception.InvalidShipmentStatusTransitionException
import com.example.saga.shipping.domain.vo.OrderId
import com.example.saga.shipping.domain.vo.ShipmentId
import com.example.saga.shipping.domain.vo.ShipmentStatus

class Shipment private constructor(
    val id: ShipmentId,
    val orderId: OrderId,
    val status: ShipmentStatus,
) {

    fun ship(): Shipment = transitionTo(ShipmentStatus.SHIPPED)

    fun cancel(): Shipment = transitionTo(ShipmentStatus.CANCELLED)

    private fun transitionTo(target: ShipmentStatus): Shipment {
        if (!status.canTransitionTo(target)) throw InvalidShipmentStatusTransitionException(status, target)
        return Shipment(id, orderId, target)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shipment) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Shipment(id=$id, orderId=$orderId, status=$status)"

    companion object {
        fun register(id: ShipmentId, orderId: OrderId): Shipment = Shipment(id, orderId, ShipmentStatus.REGISTERED)

        fun registerCancelled(id: ShipmentId, orderId: OrderId): Shipment = Shipment(id, orderId, ShipmentStatus.CANCELLED)

        fun restore(id: ShipmentId, orderId: OrderId, status: ShipmentStatus): Shipment = Shipment(id, orderId, status)
    }
}
