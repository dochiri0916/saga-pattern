package com.example.saga.inventory.domain

import com.example.saga.inventory.domain.exception.InsufficientStockException
import com.example.saga.inventory.domain.exception.InvalidStockAmountException
import com.example.saga.inventory.domain.vo.StockId
import com.example.saga.inventory.domain.vo.StockName
import com.example.saga.inventory.domain.vo.StockPrice
import com.example.saga.inventory.domain.vo.StockQuantity

class Stock private constructor(
    val id: StockId,
    val name: StockName,
    val quantity: StockQuantity,
    val price: StockPrice,
) {

    fun increase(amount: Int): Stock {
        if (amount <= 0) throw InvalidStockAmountException(amount)
        return copy(quantity = quantity + amount)
    }

    fun decrease(amount: Int): Stock {
        if (amount <= 0) throw InvalidStockAmountException(amount)
        if (quantity.isLessThan(amount)) {
            throw InsufficientStockException(quantity.value, amount)
        }
        return copy(quantity = quantity - amount)
    }

    private fun copy(quantity: StockQuantity = this.quantity): Stock = Stock(id, name, quantity, price)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stock) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Stock(id=$id, name=$name, quantity=$quantity, price=$price)"

    companion object {
        fun register(
            id: StockId,
            name: StockName,
            quantity: StockQuantity,
            price: StockPrice,
        ): Stock = Stock(id, name, quantity, price)

        fun restore(
            id: StockId,
            name: StockName,
            quantity: StockQuantity,
            price: StockPrice,
        ): Stock = Stock(id, name, quantity, price)
    }
}
