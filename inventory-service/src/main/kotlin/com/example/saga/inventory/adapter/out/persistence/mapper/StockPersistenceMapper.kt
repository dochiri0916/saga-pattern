package com.example.saga.inventory.adapter.out.persistence.mapper

import com.example.saga.inventory.adapter.out.persistence.entity.StockEntity
import com.example.saga.inventory.domain.stock.Stock
import com.example.saga.inventory.domain.stock.vo.StockId
import com.example.saga.inventory.domain.stock.vo.StockName
import com.example.saga.inventory.domain.stock.vo.StockPrice
import com.example.saga.inventory.domain.stock.vo.StockQuantity

fun Stock.toEntity(): StockEntity = StockEntity.of(
    publicId = id.value,
    name = name.value,
    quantity = quantity.value,
    price = price.value,
)

fun StockEntity.toDomain(): Stock = Stock.restore(
    id = StockId.of(publicId),
    name = StockName.of(name),
    quantity = StockQuantity.of(quantity),
    price = StockPrice.of(price),
)
