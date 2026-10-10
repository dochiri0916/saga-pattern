package com.example.saga.shipping.domain.exception

import com.example.saga.shipping.domain.vo.ShipmentStatus

class InvalidShipmentStatusTransitionException(
    val currentStatus: ShipmentStatus,
    val targetStatus: ShipmentStatus,
) : ShipmentDomainException()
