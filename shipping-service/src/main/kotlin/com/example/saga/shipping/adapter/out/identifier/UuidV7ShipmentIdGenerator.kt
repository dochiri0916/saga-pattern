package com.example.saga.shipping.adapter.out.identifier

import com.example.saga.shipping.application.port.out.ShipmentIdGenerator
import com.example.saga.shipping.domain.vo.ShipmentId
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Component

@Component
class UuidV7ShipmentIdGenerator : ShipmentIdGenerator {
    override fun generate(): ShipmentId = ShipmentId.of(UuidCreator.getTimeOrderedEpoch())
}
