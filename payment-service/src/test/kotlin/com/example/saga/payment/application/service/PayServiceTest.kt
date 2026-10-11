package com.example.saga.payment.application.service

import com.example.saga.payment.adapter.out.identifier.UuidV7PaymentIdGenerator
import com.example.saga.payment.application.command.CancelOrderPaymentCommand
import com.example.saga.payment.application.command.PayCommand
import com.example.saga.payment.application.exception.PaymentAlreadyExistsException
import com.example.saga.payment.application.exception.PaymentRejectedException
import com.example.saga.payment.application.exception.PaymentRejectionReason
import com.example.saga.payment.application.port.out.OrderPaymentCancellationRepository
import com.example.saga.payment.application.port.out.PaymentGateway
import com.example.saga.payment.application.port.out.PaymentRepository
import com.example.saga.payment.domain.payment.Payment
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentAmount
import com.example.saga.payment.domain.payment.vo.PaymentId
import com.example.saga.payment.domain.payment.vo.PaymentStatus
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PayServiceTest {

    private val payments = InMemoryPaymentRepository()
    private val cancellations = InMemoryOrderPaymentCancellationRepository()
    private val gateway = FakeGateway()
    private val executor = PaymentStepExecutor(cancellations, UuidV7PaymentIdGenerator(), payments, RecordingAuditTrail())
    private val payService = PayService(executor, gateway)
    private val cancelService = CancelOrderPaymentService(executor, gateway)

    private val orderId = OrderReferenceId.of(UuidCreator.getTimeOrderedEpoch())

    private fun pay(amount: Long = 1000) = payService.pay(PayCommand(orderId.value, amount))

    private fun cancel() = cancelService.cancel(CancelOrderPaymentCommand(orderId.value))

    private fun status(): PaymentStatus? = payments.findByOrderId(orderId)?.status

    private fun rejectionOf(block: () -> Unit): PaymentRejectionReason = assertFailsWith<PaymentRejectedException> { block() }.reason

    @Test
    fun `결제사가 승인하면 결제가 완료된다`() {
        val result = pay()

        assertEquals("COMPLETED", result.status)
        assertEquals(PaymentStatus.COMPLETED, status())
        assertEquals(1, gateway.approvals)
    }

    @Test
    fun `같은 주문으로 다시 요청하면 새로 승인받지 않고 같은 결제를 돌려준다`() {
        val first = pay()
        val second = pay()

        assertEquals(first.paymentId, second.paymentId)
        assertEquals(1, gateway.approvals)
    }

    @Test
    fun `결제사가 거절하면 거절로 남고 다시 요청해도 같은 사유로 거절한다`() {
        gateway.approve = { false }

        assertEquals(PaymentRejectionReason.GATEWAY_DECLINED, rejectionOf { pay() })
        assertEquals(PaymentStatus.REJECTED, status())
        assertEquals(PaymentRejectionReason.GATEWAY_DECLINED, rejectionOf { pay() })
        assertEquals(1, gateway.approvals)
    }

    @Test
    fun `금액이 유효하지 않으면 결제를 등록하지 않고 거절한다`() {
        assertEquals(PaymentRejectionReason.INVALID_AMOUNT, rejectionOf { pay(Long.MAX_VALUE) })
        assertEquals(PaymentRejectionReason.INVALID_AMOUNT, rejectionOf { pay(0) })
        assertEquals(null, status())
        assertEquals(0, gateway.approvals)
    }

    @Test
    fun `먼저 취소된 주문은 결제사에 묻지 않고 거절한다`() {
        cancel()

        assertEquals(PaymentRejectionReason.ORDER_CANCELLED, rejectionOf { pay() })
        assertEquals(null, status())
        assertEquals(0, gateway.approvals)
        assertEquals(emptyList(), gateway.cancelled)
    }

    @Test
    fun `승인을 기다리는 사이 취소되면 받은 승인을 다시 취소하고 거절한다`() {
        gateway.approve = {
            cancel()
            true
        }

        assertEquals(PaymentRejectionReason.ORDER_CANCELLED, rejectionOf { pay() })
        assertEquals(PaymentStatus.CANCELLED, status())
        assertEquals(2, gateway.cancelled.size, "취소 요청과 늦게 온 승인의 취소가 각각 결제사에 간다")
    }

    @Test
    fun `승인을 기다리는 사이 취소됐는데 결제사도 거절했다면 결제사 취소는 취소 요청의 것 한 번뿐이다`() {
        gateway.approve = {
            cancel()
            false
        }

        assertEquals(PaymentRejectionReason.ORDER_CANCELLED, rejectionOf { pay() })
        assertEquals(PaymentStatus.CANCELLED, status())
        assertEquals(1, gateway.cancelled.size)
    }

    @Test
    fun `승인 요청 도중 실패해 남은 결제는 다시 결제하지 않고 취소 요청으로 정리된다`() {
        gateway.approve = { throw IllegalStateException("결제사 응답 없음") }
        assertFailsWith<IllegalStateException> { pay() }
        assertEquals(PaymentStatus.PENDING, status())

        gateway.approve = { true }
        assertFailsWith<PaymentAlreadyExistsException> { pay() }

        cancel()
        assertEquals(PaymentStatus.CANCELLED, status())
        assertEquals(1, gateway.cancelled.size)
    }

    @Test
    fun `완료된 결제를 취소하면 결제사에도 취소하고 이후 결제 요청을 거절한다`() {
        pay()

        cancel()

        assertEquals(PaymentStatus.CANCELLED, status())
        assertEquals(1, gateway.cancelled.size)
        assertEquals(PaymentRejectionReason.ORDER_CANCELLED, rejectionOf { pay() })
    }

    @Test
    fun `취소를 다시 요청하면 상태는 그대로 두고 결제사 취소를 다시 한다`() {
        pay()
        cancel()

        cancel()

        assertEquals(PaymentStatus.CANCELLED, status())
        assertEquals(2, gateway.cancelled.size)
    }

    @Test
    fun `결제사가 거절했던 결제는 취소 요청이 와도 결제사에 취소하지 않는다`() {
        gateway.approve = { false }
        rejectionOf { pay() }

        cancel()

        assertEquals(PaymentStatus.REJECTED, status())
        assertEquals(emptyList(), gateway.cancelled)
    }
}

private class FakeGateway : PaymentGateway {
    var approve: () -> Boolean = { true }
    var approvals = 0
    val cancelled = mutableListOf<PaymentId>()

    override fun approve(orderId: OrderReferenceId, amount: PaymentAmount): Boolean {
        approvals++
        return approve()
    }

    override fun cancel(payment: Payment) {
        cancelled += payment.id
    }
}

private class InMemoryPaymentRepository : PaymentRepository {
    private val payments = LinkedHashMap<PaymentId, Payment>()

    override fun save(payment: Payment): Payment = payment.also { payments[it.id] = it }
    override fun findById(id: PaymentId): Payment? = payments[id]
    override fun findByIdForUpdate(id: PaymentId): Payment? = payments[id]
    override fun findByOrderId(orderId: OrderReferenceId): Payment? = payments.values.find { it.orderId == orderId }
    override fun findByOrderIdForUpdate(orderId: OrderReferenceId): Payment? = findByOrderId(orderId)
}

private class InMemoryOrderPaymentCancellationRepository : OrderPaymentCancellationRepository {
    private val cancelled = HashSet<OrderReferenceId>()

    override fun saveIfAbsent(orderId: OrderReferenceId) {
        cancelled += orderId
    }

    override fun existsForUpdate(orderId: OrderReferenceId): Boolean = orderId in cancelled
    override fun deleteCancelledBefore(before: Instant, limit: Int): Int = 0
}
