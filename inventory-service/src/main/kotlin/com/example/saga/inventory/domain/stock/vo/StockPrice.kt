package com.example.saga.inventory.domain.stock.vo

import com.example.saga.inventory.domain.stock.exception.NegativeStockPriceException

@JvmInline
value class StockPrice private constructor(
    val value: Int
) {

    init {
        if (value < 0) throw NegativeStockPriceException(value)
    }

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: Int): StockPrice = StockPrice(value)
    }
}
