package com.example.saga.order.application.port.out

import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.domain.order.vo.StockReferenceId

interface StockSnapshotRepository {

    fun saveIfNewer(stockSnapshot: StockSnapshot): Boolean

    fun findById(stockReferenceId: StockReferenceId): StockSnapshot?
}
