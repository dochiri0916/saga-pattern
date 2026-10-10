package com.example.saga.shipping.application.port.out

import com.example.saga.shipping.domain.Shipment
import com.example.saga.shipping.domain.vo.OrderId

interface ShipmentRepository {
    fun save(shipment: Shipment): Shipment

    fun saveIfAbsent(shipment: Shipment): Boolean

    fun saveIfAbsentAndLock(shipment: Shipment): Shipment

    fun findByOrderId(orderId: OrderId): Shipment?

    fun findByOrderIdForUpdate(orderId: OrderId): Shipment?
}
