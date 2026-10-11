package com.example.saga.inventory.adapter.out.persistence.mapper

import com.example.saga.inventory.adapter.out.persistence.entity.OrderStockRecordEntity
import com.example.saga.inventory.domain.orderstock.OrderStockRecord
import com.example.saga.inventory.domain.orderstock.vo.OrderReferenceId
import com.example.saga.inventory.domain.orderstock.vo.OrderStockStatus

fun OrderStockRecordEntity.toRecord(): OrderStockRecord =
    OrderStockRecord(OrderReferenceId.of(orderId), OrderStockStatus.valueOf(status))
