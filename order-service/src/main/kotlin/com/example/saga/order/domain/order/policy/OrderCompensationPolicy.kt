package com.example.saga.order.domain.order.policy

enum class OrderFailure { STOCK_REJECTED, PAYMENT_REJECTED, STOCK_UNCERTAIN, PAYMENT_UNCERTAIN, COMPLETED_CANCEL }
enum class OrderCompensation { CANCEL_SHIPMENT, CANCEL_PAYMENT, RESTORE_STOCK, DONE }

object OrderCompensationPolicy {
    fun after(failure: OrderFailure): OrderCompensation = when (failure) {
        OrderFailure.STOCK_REJECTED -> OrderCompensation.DONE
        OrderFailure.PAYMENT_REJECTED, OrderFailure.STOCK_UNCERTAIN -> OrderCompensation.RESTORE_STOCK
        OrderFailure.PAYMENT_UNCERTAIN -> OrderCompensation.CANCEL_PAYMENT
        OrderFailure.COMPLETED_CANCEL -> OrderCompensation.CANCEL_SHIPMENT
    }

    fun afterCompensation(step: OrderCompensation): OrderCompensation = when (step) {
        OrderCompensation.CANCEL_SHIPMENT -> OrderCompensation.CANCEL_PAYMENT
        OrderCompensation.CANCEL_PAYMENT -> OrderCompensation.RESTORE_STOCK
        OrderCompensation.RESTORE_STOCK, OrderCompensation.DONE -> OrderCompensation.DONE
    }
}
