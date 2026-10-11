package com.example.saga.shipping.domain.shipment.vo

import java.util.UUID

@JvmInline
value class ShipmentId private constructor(
    val value: UUID
) {
    override fun toString(): String = value.toString()

    companion object {
        fun of(value: UUID): ShipmentId = ShipmentId(value)
    }
}
