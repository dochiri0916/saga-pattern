package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.domain.orderstock.OrderStockRecord
import com.example.saga.inventory.domain.orderstock.vo.OrderReferenceId
import com.example.saga.inventory.domain.orderstock.vo.OrderStockStatus
import java.time.Instant

interface OrderStockRecordRepository {

    fun saveIfAbsent(record: OrderStockRecord): Boolean

    fun saveIfAbsentAndLock(record: OrderStockRecord): OrderStockRecord

    fun save(record: OrderStockRecord)

    fun findByOrderId(orderId: OrderReferenceId): OrderStockRecord?

    fun deleteUpdatedBefore(statuses: List<OrderStockStatus>, before: Instant, limit: Int): Int
}
