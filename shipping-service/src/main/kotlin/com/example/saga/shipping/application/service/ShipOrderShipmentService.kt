package com.example.saga.shipping.application.service

import com.example.saga.shipping.application.command.ShipOrderShipmentCommand
import com.example.saga.shipping.application.exception.ShipmentNotFoundException
import com.example.saga.shipping.application.port.`in`.ShipOrderShipmentUseCase
import com.example.saga.shipping.application.port.out.AuditTrail
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.shipment.vo.OrderReferenceId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShipOrderShipmentService(
    private val shipmentRepository: ShipmentRepository,
    private val auditTrail: AuditTrail,
) : ShipOrderShipmentUseCase {

    @Transactional
    override fun ship(command: ShipOrderShipmentCommand) {
        val orderId = OrderReferenceId.of(command.orderId)
        val shipment = shipmentRepository.findByOrderIdForUpdate(orderId) ?: throw ShipmentNotFoundException(command.orderId.toString())
        shipmentRepository.save(shipment.ship())
        auditTrail.record(shipment.id.toString(), "SHIPMENT_SHIPPED", orderId.toString())
    }
}
