package com.example.saga.inventory.domain.vo

import java.util.UUID

@JvmInline
value class StockId private constructor(
    val value: UUID
) {

    override fun toString(): String = value.toString()

    companion object {
        fun of(value: UUID): StockId = StockId(value)
    }
}
