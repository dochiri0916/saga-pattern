package com.example.saga.order.domain.order.exception

import com.example.saga.order.domain.order.vo.OrderItemId
import com.example.saga.order.domain.order.vo.OrderItemStatus
import com.example.saga.order.domain.order.vo.OrderStatus
import com.example.saga.order.domain.order.vo.StockReferenceId

sealed class OrderDomainException(cause: Throwable? = null) : RuntimeException(cause)

class InvalidOrderQuantityException(val quantity: Int) : OrderDomainException()

class NegativeAmountException(val subject: Subject, val value: Long) : OrderDomainException() {
    enum class Subject { ORDER_AMOUNT, UNIT_PRICE }
}

class OrderAmountOverflowException(cause: ArithmeticException) : OrderDomainException(cause)

class OrderItemsRequiredException : OrderDomainException()

class OrderItemsLimitExceededException(val size: Int, val maxSize: Int) : OrderDomainException()

class DuplicateOrderItemException(val stockReferenceId: StockReferenceId) : OrderDomainException()

class DuplicateOrderItemIdException(val itemId: OrderItemId) : OrderDomainException()

class OrderItemNotFoundException(val itemId: OrderItemId) : OrderDomainException()

class InvalidOrderStatusTransitionException(val currentStatus: OrderStatus, val targetStatus: OrderStatus) :
    OrderDomainException()

class InvalidOrderItemStatusTransitionException(val currentStatus: OrderItemStatus, val targetStatus: OrderItemStatus) :
    OrderDomainException()

class OrderItemChangeNotAllowedException(val orderStatus: OrderStatus) : OrderDomainException()
