package com.example.saga.shipping.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ShipmentJpaRepository : JpaRepository<ShipmentEntity, Long> {

    fun findByPublicId(publicId: UUID): ShipmentEntity?

    fun findByOrderId(orderId: UUID): ShipmentEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByOrderId(orderId: UUID): ShipmentEntity?

    @Modifying
    @Query(
        value = "insert ignore into shipments (public_id, order_id, status) values (:publicId, :orderId, :status)",
        nativeQuery = true,
    )
    fun insertIgnore(publicId: UUID, orderId: UUID, status: String): Int

    @Modifying
    @Query(
        value = "insert into shipments (public_id, order_id, status) values (:publicId, :orderId, :status) " +
            "on duplicate key update order_id = order_id",
        nativeQuery = true,
    )
    fun insertOrLock(publicId: UUID, orderId: UUID, status: String): Int
}
