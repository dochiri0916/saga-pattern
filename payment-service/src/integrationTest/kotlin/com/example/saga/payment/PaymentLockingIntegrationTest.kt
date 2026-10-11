package com.example.saga.payment

import com.example.saga.payment.application.command.CancelOrderPaymentCommand
import com.example.saga.payment.application.command.PayCommand
import com.example.saga.payment.application.exception.PaymentRejectedException
import com.example.saga.payment.application.port.`in`.CancelOrderPaymentUseCase
import com.example.saga.payment.application.port.`in`.PayUseCase
import com.example.saga.payment.application.port.out.PaymentRepository
import com.example.saga.payment.domain.payment.vo.OrderReferenceId
import com.example.saga.payment.domain.payment.vo.PaymentStatus
import com.github.f4b6a3.uuid.UuidCreator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [PaymentIntegrationTestConfig::class], properties = [MYSQL_URL, CREATE_TABLES, POOL_SIZE, NO_OUTBOX, NO_CONSUMER])
class PaymentLockingIntegrationTest @Autowired constructor(
    private val payUseCase: PayUseCase,
    private val cancelOrderPaymentUseCase: CancelOrderPaymentUseCase,
    private val paymentRepository: PaymentRepository,
    private val gateway: RecordingPaymentGateway,
) {

    @Test
    fun `결제와 취소가 동시에 와도 취소된 주문에 살아 있는 결제가 남지 않는다`() {
        repeat(40) {
            val orderId = UuidCreator.getTimeOrderedEpoch()

            val (pay, cancel) = race(
                { payUseCase.pay(PayCommand(orderId, 1000)); Unit },
                { cancelOrderPaymentUseCase.cancel(CancelOrderPaymentCommand(orderId)) },
            )

            cancel.getOrThrow()

            pay.exceptionOrNull()?.let { assertTrue(it is PaymentRejectedException, "예상하지 못한 실패: $it") }
            val status = paymentRepository.findByOrderId(OrderReferenceId.of(orderId))?.status
            assertTrue(status == null || status == PaymentStatus.CANCELLED, "취소된 주문의 결제가 $status 상태로 남았다")

            if (OrderReferenceId.of(orderId) in gateway.approved) assertTrue(OrderReferenceId.of(orderId) in gateway.cancelled, "승인된 결제가 결제사에 취소되지 않았다")
        }
    }

    @Test
    fun `같은 주문의 취소가 동시에 여러 번 와도 모두 성공하고 결제는 취소된다`() {
        val orderId = UuidCreator.getTimeOrderedEpoch()
        payUseCase.pay(PayCommand(orderId, 1000))

        val results = race(*Array(8) { { cancelOrderPaymentUseCase.cancel(CancelOrderPaymentCommand(orderId)) } })

        results.forEach { it.getOrThrow() }
        assertEquals(PaymentStatus.CANCELLED, paymentRepository.findByOrderId(OrderReferenceId.of(orderId))?.status)
    }

    @Test
    fun `같은 주문의 결제 요청이 동시에 와도 결제는 하나만 만들어진다`() {
        val orderId = UuidCreator.getTimeOrderedEpoch()

        val results = race(*Array(8) { { payUseCase.pay(PayCommand(orderId, 1000)) } })

        val paymentIds = results.mapNotNull { it.getOrNull()?.paymentId }.toSet()
        assertEquals(1, paymentIds.size, "성공한 요청은 모두 같은 결제를 돌려줘야 한다")
        assertEquals(PaymentStatus.COMPLETED, paymentRepository.findByOrderId(OrderReferenceId.of(orderId))?.status)
    }
}
