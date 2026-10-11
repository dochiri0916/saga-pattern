package com.example.saga.order.adapter.out.persistence.mapper

import com.example.saga.order.adapter.out.persistence.entity.StockSnapshotEntity
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.domain.order.vo.StockReferenceId

fun StockSnapshotEntity.toReadModel(): StockSnapshot = StockSnapshot(
    stockReferenceId = StockReferenceId.of(stockId),
    name = name,
    price = price,
    quantity = quantity,
    version = version,
)
