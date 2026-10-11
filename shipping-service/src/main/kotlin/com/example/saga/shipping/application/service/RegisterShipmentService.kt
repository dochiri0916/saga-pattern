package com.example.saga.shipping.application.service

import com.example.saga.shipping.application.command.RegisterShipmentCommand
import com.example.saga.shipping.application.port.`in`.RegisterShipmentUseCase
import com.example.saga.shipping.application.port.out.AuditTrail
import com.example.saga.shipping.application.port.out.ShipmentIdGenerator
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.shipment.Shipment
import com.example.saga.shipping.domain.shipment.vo.OrderReferenceId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(RegisterShipmentService::class.java)

@Service
class RegisterShipmentService(
    private val shipmentIdGenerator: ShipmentIdGenerator,
    private val shipmentRepository: ShipmentRepository,
    private val auditTrail: AuditTrail,
) : RegisterShipmentUseCase {

    @Transactional
    override fun register(command: RegisterShipmentCommand) {
        val orderId = OrderReferenceId.of(command.orderId)
        val shipment = Shipment.register(shipmentIdGenerator.generate(), orderId)
        if (shipmentRepository.saveIfAbsent(shipment)) {
            auditTrail.record(shipment.id.toString(), "SHIPMENT_REGISTERED", orderId.toString())
            log.info("배송을 등록했습니다. orderId={}, shipmentId={}", orderId, shipment.id)
        } else {
            log.info("이미 배송이 있는 주문이라서 등록하지 않습니다. orderId={}", orderId)
        }
    }
}
