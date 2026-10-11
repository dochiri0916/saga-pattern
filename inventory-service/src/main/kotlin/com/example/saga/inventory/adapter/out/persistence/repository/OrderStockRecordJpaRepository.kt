package com.example.saga.inventory.adapter.out.persistence.repository

import com.example.saga.inventory.adapter.out.persistence.entity.OrderStockRecordEntity
import jakarta.persistence.LockModeType
import java.time.Instant
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface OrderStockRecordJpaRepository : JpaRepository<OrderStockRecordEntity, UUID> {

    @Modifying
    @Query(
        value = "insert ignore into order_stock_records (order_id, status, updated_at) values (:orderId, :status, :updatedAt)",
        nativeQuery = true,
    )
    fun insertIgnore(orderId: UUID, status: String, updatedAt: Instant): Int

    @Modifying
    @Query(
        value = "insert into order_stock_records (order_id, status, updated_at) values (:orderId, :status, :updatedAt) " +
            "on duplicate key update order_id = order_id",
        nativeQuery = true,
    )
    fun insertOrLock(orderId: UUID, status: String, updatedAt: Instant): Int

    @Transactional
    @Modifying
    @Query(
        value = "delete from order_stock_records where status in (:statuses) and updated_at < :cutoff limit :batchSize",
        nativeQuery = true,
    )
    fun deleteUpdatedBefore(statuses: Collection<String>, cutoff: Instant, batchSize: Int): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByOrderId(orderId: UUID): OrderStockRecordEntity?
}
