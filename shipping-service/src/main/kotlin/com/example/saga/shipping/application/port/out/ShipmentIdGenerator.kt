package com.example.saga.shipping.application.port.out

import com.example.saga.shipping.domain.vo.ShipmentId

interface ShipmentIdGenerator {
    fun generate(): ShipmentId
}
