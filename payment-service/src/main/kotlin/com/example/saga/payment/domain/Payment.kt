package com.example.saga.payment.domain

import com.example.saga.payment.domain.exception.InvalidPaymentStatusTransitionException
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import com.example.saga.payment.domain.vo.PaymentId
import com.example.saga.payment.domain.vo.PaymentStatus

class Payment private constructor(
    val id: PaymentId,
    val orderId: OrderId,
    val amount: PaymentAmount,
    val status: PaymentStatus,
) {

    fun approve(): Payment = transitionTo(PaymentStatus.COMPLETED)

    fun reject(): Payment = transitionTo(PaymentStatus.REJECTED)

    fun cancel(): Payment = transitionTo(PaymentStatus.CANCELLED)

    private fun transitionTo(target: PaymentStatus): Payment {
        if (!status.canTransitionTo(target)) throw InvalidPaymentStatusTransitionException(status, target)
        return Payment(id, orderId, amount, target)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Payment) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Payment(id=$id, orderId=$orderId, amount=$amount, status=$status)"

    companion object {

        fun register(id: PaymentId, orderId: OrderId, amount: PaymentAmount): Payment =
            Payment(id, orderId, amount, PaymentStatus.PENDING)

        fun restore(id: PaymentId, orderId: OrderId, amount: PaymentAmount, status: PaymentStatus): Payment =
            Payment(id, orderId, amount, status)
    }
}
