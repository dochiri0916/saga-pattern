package com.example.saga.inventory.domain.vo

import com.example.saga.inventory.domain.exception.StockNameRequiredException

@JvmInline
value class StockName private constructor(
    val value: String
) {

    init {
        if (value.isBlank()) throw StockNameRequiredException()
    }

    override fun toString(): String = value

    companion object {
        fun of(value: String): StockName = StockName(value)
    }
}
