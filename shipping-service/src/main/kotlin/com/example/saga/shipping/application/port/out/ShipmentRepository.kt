package com.example.saga.shipping.application.port.out

import com.example.saga.shipping.domain.shipment.Shipment
import com.example.saga.shipping.domain.shipment.vo.OrderReferenceId

interface ShipmentRepository {
    fun save(shipment: Shipment): Shipment

    fun saveIfAbsent(shipment: Shipment): Boolean

    fun saveIfAbsentAndLock(shipment: Shipment): Shipment

    fun findByOrderId(orderId: OrderReferenceId): Shipment?

    fun findByOrderIdForUpdate(orderId: OrderReferenceId): Shipment?
}
