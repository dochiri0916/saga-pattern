package com.example.saga.inventory.domain.stock.vo

import com.example.saga.inventory.domain.stock.exception.NegativeStockQuantityException
import com.example.saga.inventory.domain.stock.exception.StockOverflowException

@JvmInline
value class StockQuantity private constructor(
    val value: Int
) {

    init {
        if (value < 0) throw NegativeStockQuantityException(value)
    }

    operator fun plus(amount: Int): StockQuantity = StockQuantity(exact(amount, Math::addExact))

    operator fun minus(amount: Int): StockQuantity = StockQuantity(exact(amount, Math::subtractExact))

    fun isLessThan(amount: Int): Boolean = value < amount

    override fun toString(): String = value.toString()

    private inline fun exact(amount: Int, operation: (Int, Int) -> Int): Int =
        try {
            operation(value, amount)
        } catch (e: ArithmeticException) {
            throw StockOverflowException(value, amount, e)
        }

    companion object {
        fun of(value: Int): StockQuantity = StockQuantity(value)
    }
}
