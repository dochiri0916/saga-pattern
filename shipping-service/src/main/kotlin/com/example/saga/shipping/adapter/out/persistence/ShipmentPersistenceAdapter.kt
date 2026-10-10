package com.example.saga.shipping.adapter.out.persistence

import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.Shipment
import com.example.saga.shipping.domain.vo.OrderId
import com.example.saga.shipping.domain.vo.ShipmentId
import com.example.saga.shipping.domain.vo.ShipmentStatus
import org.springframework.stereotype.Repository

@Repository
class ShipmentPersistenceAdapter(
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

    override fun findByOrderId(orderId: OrderId): Shipment? =
        shipmentJpaRepository.findByOrderId(orderId.value)?.toDomain()

    override fun findByOrderIdForUpdate(orderId: OrderId): Shipment? =
        shipmentJpaRepository.findForUpdateByOrderId(orderId.value)?.toDomain()

    private fun ShipmentEntity.toDomain(): Shipment =
        Shipment.restore(ShipmentId.of(publicId), OrderId.of(orderId), ShipmentStatus.valueOf(status))
}
