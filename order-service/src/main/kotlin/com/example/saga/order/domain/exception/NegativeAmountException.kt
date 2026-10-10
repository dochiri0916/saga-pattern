package com.example.saga.order.domain.exception

class NegativeAmountException(val subject: Subject, val value: Long) : OrderDomainException() {

    enum class Subject { ORDER_AMOUNT, UNIT_PRICE }
}
