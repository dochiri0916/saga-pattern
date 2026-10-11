package com.example.saga.shipping.adapter.out.persistence.mapper

import com.example.saga.shipping.adapter.out.persistence.entity.ShipmentEntity
import com.example.saga.shipping.domain.shipment.Shipment
import com.example.saga.shipping.domain.shipment.vo.OrderReferenceId
import com.example.saga.shipping.domain.shipment.vo.ShipmentId
import com.example.saga.shipping.domain.shipment.vo.ShipmentStatus

fun ShipmentEntity.toDomain(): Shipment =
    Shipment.restore(ShipmentId.of(publicId), OrderReferenceId.of(orderId), ShipmentStatus.valueOf(status))
