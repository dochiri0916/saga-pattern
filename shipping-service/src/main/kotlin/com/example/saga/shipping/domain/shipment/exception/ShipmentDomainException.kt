package com.example.saga.shipping.domain.shipment.exception

import com.example.saga.shipping.domain.shipment.vo.ShipmentStatus

sealed class ShipmentDomainException(cause: Throwable? = null) : RuntimeException(cause)

class InvalidShipmentStatusTransitionException(
    val currentStatus: ShipmentStatus,
    val targetStatus: ShipmentStatus,
) : ShipmentDomainException()
