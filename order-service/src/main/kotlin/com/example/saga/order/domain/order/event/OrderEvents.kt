package com.example.saga.order.domain.order.event

class OrderEvents private constructor(
    private val values: List<OrderEvent>
) {

    fun asList(): List<OrderEvent> = values

    operator fun plus(other: OrderEvents): OrderEvents = of(values + other.values)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderEvents) return false
        return values == other.values
    }

    override fun hashCode(): Int = values.hashCode()

    override fun toString(): String = "OrderEvents($values)"

    companion object {
        fun of(values: List<OrderEvent>): OrderEvents = OrderEvents(java.util.List.copyOf(values))

        fun empty(): OrderEvents = of(emptyList())
    }
}
