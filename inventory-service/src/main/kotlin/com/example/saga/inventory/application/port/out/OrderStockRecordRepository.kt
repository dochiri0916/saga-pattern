package com.example.saga.inventory.application.port.out

import com.example.saga.inventory.application.saga.OrderStockRecord
import com.example.saga.inventory.application.saga.OrderStockStatus
import com.example.saga.inventory.domain.vo.OrderId
import java.time.Instant

interface OrderStockRecordRepository {

    fun saveIfAbsent(record: OrderStockRecord): Boolean

    fun saveIfAbsentAndLock(record: OrderStockRecord): OrderStockRecord

    fun save(record: OrderStockRecord)

    fun findByOrderId(orderId: OrderId): OrderStockRecord?

    fun deleteUpdatedBefore(statuses: List<OrderStockStatus>, before: Instant, limit: Int): Int
}
