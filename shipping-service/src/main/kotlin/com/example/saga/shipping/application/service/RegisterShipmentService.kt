package com.example.saga.shipping.application.service

import com.example.saga.shipping.application.port.`in`.command.RegisterShipmentCommand
import com.example.saga.shipping.application.port.`in`.usecase.RegisterShipmentUseCase
import com.example.saga.shipping.application.port.out.ProcessedEventRepository
import com.example.saga.shipping.application.port.out.ShipmentIdGenerator
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.Shipment
import com.example.saga.shipping.domain.vo.OrderId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(RegisterShipmentService::class.java)

@Service
class RegisterShipmentService(
    private val processedEventRepository: ProcessedEventRepository,
    private val shipmentIdGenerator: ShipmentIdGenerator,
    private val shipmentRepository: ShipmentRepository,
) : RegisterShipmentUseCase {

    @Transactional
    override fun register(command: RegisterShipmentCommand) {
        val orderId = OrderId.of(command.orderId)
        if (!processedEventRepository.saveIfAbsent(command.eventId)) {
            log.info("이미 처리한 이벤트라서 건너뜁니다. eventId={}, orderId={}", command.eventId, orderId)
            return
        }

        val shipment = Shipment.register(shipmentIdGenerator.generate(), orderId)
        if (shipmentRepository.saveIfAbsent(shipment)) {
            log.info("배송을 등록했습니다. orderId={}, shipmentId={}", orderId, shipment.id)
        } else {
            log.info("이미 배송이 있는 주문이라서 등록하지 않습니다. orderId={}", orderId)
        }
    }
}
