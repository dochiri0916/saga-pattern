package com.example.saga.order.domain

import com.example.saga.order.domain.exception.DuplicateOrderItemException
import com.example.saga.order.domain.exception.DuplicateOrderItemIdException
import com.example.saga.order.domain.exception.OrderItemNotFoundException
import com.example.saga.order.domain.exception.OrderItemsLimitExceededException
import com.example.saga.order.domain.exception.OrderItemsRequiredException
import com.example.saga.order.domain.vo.OrderAmount
import com.example.saga.order.domain.vo.OrderItemId
import com.example.saga.order.domain.vo.OrderItemStatus
import com.example.saga.order.domain.vo.StockReferenceId
import java.util.Collections

class OrderItems private constructor(
    private val values: List<OrderItem>
) {

    val totalAmount: OrderAmount

    val remainingAmount: OrderAmount
        get() = amountOf { it == OrderItemStatus.ORDERED }

    val closedAmount: OrderAmount
        get() = amountOf { it != OrderItemStatus.ORDERED }

    init {
        if (values.isEmpty()) throw OrderItemsRequiredException()
        if (values.size > MAX_SIZE) throw OrderItemsLimitExceededException(values.size, MAX_SIZE)
        val seenStocks = HashSet<StockReferenceId>(values.size)
        val seenIds = HashSet<OrderItemId>(values.size)
        values.forEach {
            if (!seenStocks.add(it.stockReferenceId)) throw DuplicateOrderItemException(it.stockReferenceId)
            if (!seenIds.add(it.id)) throw DuplicateOrderItemIdException(it.id)
        }
        totalAmount = values.fold(OrderAmount.ZERO) { total, item -> total + item.amount }
    }

    fun asList(): List<OrderItem> = values

    fun get(id: OrderItemId): OrderItem = values.firstOrNull { it.id == id } ?: throw OrderItemNotFoundException(id)

    fun replace(item: OrderItem): OrderItems = of(values.map { if (it.id == item.id) item else it })

    fun isAllCancelled(): Boolean = values.all { it.status == OrderItemStatus.CANCELLED }

    fun isAllClosed(): Boolean = values.none { it.status == OrderItemStatus.ORDERED }

    private fun amountOf(matches: (OrderItemStatus) -> Boolean): OrderAmount =
        values.filter { matches(it.status) }.fold(OrderAmount.ZERO) { total, item -> total + item.amount }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItems) return false
        return values == other.values
    }

    override fun hashCode(): Int = values.hashCode()

    override fun toString(): String = "OrderItems(size=${values.size}, totalAmount=$totalAmount)"

    companion object {
        const val MAX_SIZE = 500

        fun of(values: List<OrderItem>): OrderItems = OrderItems(Collections.unmodifiableList(ArrayList(values)))
    }
}
