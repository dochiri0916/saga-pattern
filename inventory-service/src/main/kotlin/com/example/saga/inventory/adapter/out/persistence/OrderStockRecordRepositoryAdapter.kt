package com.example.saga.inventory.adapter.out.persistence

import com.example.saga.inventory.adapter.out.persistence.mapper.toRecord
import com.example.saga.inventory.adapter.out.persistence.repository.OrderStockRecordJpaRepository
import com.example.saga.inventory.application.port.out.OrderStockRecordRepository
import com.example.saga.inventory.domain.orderstock.OrderStockRecord
import com.example.saga.inventory.domain.orderstock.vo.OrderReferenceId
import com.example.saga.inventory.domain.orderstock.vo.OrderStockStatus
import java.time.Clock
import java.time.Instant
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class OrderStockRecordRepositoryAdapter(
    private val orderStockRecordJpaRepository: OrderStockRecordJpaRepository,
    private val clock: Clock,
) : OrderStockRecordRepository {

    override fun saveIfAbsent(record: OrderStockRecord): Boolean =
        orderStockRecordJpaRepository.insertIgnore(record.orderId.value, record.status.name, clock.instant()) == 1

    override fun saveIfAbsentAndLock(record: OrderStockRecord): OrderStockRecord {
        orderStockRecordJpaRepository.insertOrLock(record.orderId.value, record.status.name, clock.instant())

        return checkNotNull(orderStockRecordJpaRepository.findForUpdateByOrderId(record.orderId.value)) {
            "방금 저장했거나 잠근 주문별 재고 기록이 없습니다. orderId=${record.orderId}"
        }.toRecord()
    }

    override fun save(record: OrderStockRecord) {
        val entity = checkNotNull(orderStockRecordJpaRepository.findByIdOrNull(record.orderId.value)) {
            "상태를 바꿀 주문별 재고 기록이 없습니다. orderId=${record.orderId}"
        }
        entity.update(record.status.name, clock.instant())
        orderStockRecordJpaRepository.saveAndFlush(entity)
    }

    override fun deleteUpdatedBefore(statuses: List<OrderStockStatus>, before: Instant, limit: Int): Int =
        orderStockRecordJpaRepository.deleteUpdatedBefore(statuses.map { it.name }, before, limit)

    override fun findByOrderId(orderId: OrderReferenceId): OrderStockRecord? =
        orderStockRecordJpaRepository.findByIdOrNull(orderId.value)?.toRecord()

}
