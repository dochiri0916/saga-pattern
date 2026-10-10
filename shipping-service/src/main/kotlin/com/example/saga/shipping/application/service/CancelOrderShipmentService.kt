package com.example.saga.shipping.application.service

import com.example.saga.shipping.application.exception.ShipmentCancelRejectedException
import com.example.saga.shipping.application.exception.ShipmentCancelRejectionReason
import com.example.saga.shipping.application.port.`in`.command.CancelOrderShipmentCommand
import com.example.saga.shipping.application.port.`in`.usecase.CancelOrderShipmentUseCase
import com.example.saga.shipping.application.port.out.ShipmentIdGenerator
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.Shipment
import com.example.saga.shipping.domain.vo.OrderId
import com.example.saga.shipping.domain.vo.ShipmentStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(CancelOrderShipmentService::class.java)

@Service
class CancelOrderShipmentService(
    private val shipmentIdGenerator: ShipmentIdGenerator,
    private val shipmentRepository: ShipmentRepository,
) : CancelOrderShipmentUseCase {

    @Transactional
    override fun cancel(command: CancelOrderShipmentCommand) {
        val orderId = OrderId.of(command.orderId)

        val shipment = shipmentRepository.saveIfAbsentAndLock(Shipment.registerCancelled(shipmentIdGenerator.generate(), orderId))
        when (shipment.status) {
            ShipmentStatus.REGISTERED -> {
                shipmentRepository.save(shipment.cancel())
                log.info("주문의 배송을 취소했습니다. orderId={}, shipmentId={}", orderId, shipment.id)
            }

            ShipmentStatus.CANCELLED -> log.info("취소된 배송입니다. orderId={}, shipmentId={}", orderId, shipment.id)
            ShipmentStatus.SHIPPED -> {
                log.info("이미 출고해서 배송 취소를 거절했습니다. orderId={}, shipmentId={}", orderId, shipment.id)
                throw ShipmentCancelRejectedException(command.orderId.toString(), ShipmentCancelRejectionReason.ALREADY_SHIPPED)
            }
        }
    }
}
