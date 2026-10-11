package com.example.saga.shipping.adapter.out.persistence

import com.example.saga.shipping.adapter.out.persistence.entity.ShipmentEntity
import com.example.saga.shipping.adapter.out.persistence.mapper.toDomain
import com.example.saga.shipping.adapter.out.persistence.repository.ShipmentJpaRepository
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.shipment.Shipment
import com.example.saga.shipping.domain.shipment.vo.OrderReferenceId
import org.springframework.stereotype.Repository

@Repository
class ShipmentRepositoryAdapter(
    private val shipmentJpaRepository: ShipmentJpaRepository,
) : ShipmentRepository {

    override fun save(shipment: Shipment): Shipment {
        val entity = shipmentJpaRepository.findByPublicId(shipment.id.value)
            ?.apply { updateStatus(shipment.status.name) }
            ?: ShipmentEntity.of(shipment.id.value, shipment.orderId.value, shipment.status.name)
        shipmentJpaRepository.saveAndFlush(entity)
        return shipment
    }

    override fun saveIfAbsent(shipment: Shipment): Boolean =
        shipmentJpaRepository.insertIgnore(shipment.id.value, shipment.orderId.value, shipment.status.name) == 1

    override fun saveIfAbsentAndLock(shipment: Shipment): Shipment {
        shipmentJpaRepository.insertOrLock(shipment.id.value, shipment.orderId.value, shipment.status.name)

        return checkNotNull(shipmentJpaRepository.findForUpdateByOrderId(shipment.orderId.value)) {
            "방금 저장했거나 잠근 배송이 없습니다. orderId=${shipment.orderId}"
        }.toDomain()
    }

    override fun findByOrderId(orderId: OrderReferenceId): Shipment? =
        shipmentJpaRepository.findByOrderId(orderId.value)?.toDomain()

    override fun findByOrderIdForUpdate(orderId: OrderReferenceId): Shipment? =
        shipmentJpaRepository.findForUpdateByOrderId(orderId.value)?.toDomain()

}
