package com.example.saga.inventory.domain.vo

import com.example.saga.inventory.domain.exception.NegativeStockPriceException

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
