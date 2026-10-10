package com.example.saga.payment.domain

import com.example.saga.payment.domain.exception.InvalidPaymentStatusTransitionException
import com.example.saga.payment.domain.vo.OrderId
import com.example.saga.payment.domain.vo.PaymentAmount
import com.example.saga.payment.domain.vo.PaymentId
import com.example.saga.payment.domain.vo.PaymentStatus
import com.github.f4b6a3.uuid.UuidCreator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaymentTest {

    private fun pending(): Payment = Payment.register(
        PaymentId.of(UuidCreator.getTimeOrderedEpoch()),
        OrderId.of(UuidCreator.getTimeOrderedEpoch()),
        PaymentAmount.of(1000),
    )

    @Test
    fun `등록한 결제는 승인을 기다리는 상태다`() {
        assertEquals(PaymentStatus.PENDING, pending().status)
    }

    @Test
    fun `승인을 기다리는 결제는 승인, 거절, 취소될 수 있다`() {
        assertEquals(PaymentStatus.COMPLETED, pending().approve().status)
        assertEquals(PaymentStatus.REJECTED, pending().reject().status)
        assertEquals(PaymentStatus.CANCELLED, pending().cancel().status)
    }

    @Test
    fun `승인된 결제는 취소만 할 수 있다`() {
        val completed = pending().approve()

        assertEquals(PaymentStatus.CANCELLED, completed.cancel().status)
        assertFailsWith<InvalidPaymentStatusTransitionException> { completed.approve() }
        assertFailsWith<InvalidPaymentStatusTransitionException> { completed.reject() }
    }

    @Test
    fun `거절되거나 취소된 결제는 더 바뀌지 않는다`() {
        listOf(pending().reject(), pending().cancel()).forEach { finished ->
            assertFailsWith<InvalidPaymentStatusTransitionException> { finished.approve() }
            assertFailsWith<InvalidPaymentStatusTransitionException> { finished.reject() }
            assertFailsWith<InvalidPaymentStatusTransitionException> { finished.cancel() }
        }
    }
}
