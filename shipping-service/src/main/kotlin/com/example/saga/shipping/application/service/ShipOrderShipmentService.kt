package com.example.saga.shipping.application.service

import com.example.saga.shipping.application.exception.ShipmentNotFoundException
import com.example.saga.shipping.application.port.`in`.command.ShipOrderShipmentCommand
import com.example.saga.shipping.application.port.`in`.usecase.ShipOrderShipmentUseCase
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.vo.OrderId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShipOrderShipmentService(
    private val shipmentRepository: ShipmentRepository,
) : ShipOrderShipmentUseCase {

    @Transactional
    override fun ship(command: ShipOrderShipmentCommand) {
        val orderId = OrderId.of(command.orderId)
        val shipment = shipmentRepository.findByOrderIdForUpdate(orderId) ?: throw ShipmentNotFoundException(command.orderId.toString())
        shipmentRepository.save(shipment.ship())
    }
}
