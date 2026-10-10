package com.example.saga.shipping

import com.example.saga.shipping.application.exception.ShipmentCancelRejectedException
import com.example.saga.shipping.application.port.`in`.command.CancelOrderShipmentCommand
import com.example.saga.shipping.application.port.`in`.command.RegisterShipmentCommand
import com.example.saga.shipping.application.port.`in`.command.ShipOrderShipmentCommand
import com.example.saga.shipping.application.port.`in`.usecase.CancelOrderShipmentUseCase
import com.example.saga.shipping.application.port.`in`.usecase.RegisterShipmentUseCase
import com.example.saga.shipping.application.port.`in`.usecase.ShipOrderShipmentUseCase
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.vo.OrderId
import com.example.saga.shipping.domain.vo.ShipmentStatus
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(classes = [ShippingIntegrationTestConfig::class], properties = [MYSQL_URL, CREATE_TABLES, POOL_SIZE, NO_OUTBOX, NO_CONSUMER])
class ShipmentLockingIntegrationTest @Autowired constructor(
    private val registerShipmentUseCase: RegisterShipmentUseCase,
    private val shipOrderShipmentUseCase: ShipOrderShipmentUseCase,
    private val cancelOrderShipmentUseCase: CancelOrderShipmentUseCase,
    private val shipmentRepository: ShipmentRepository,
) {

    private fun newId(): UUID = UuidCreator.getTimeOrderedEpoch()

    private fun status(orderId: UUID): ShipmentStatus? = shipmentRepository.findByOrderId(OrderId.of(orderId))?.status

    @Test
    fun `출고와 취소가 동시에 와도 둘 중 하나만 받아들여진다`() {
        var shipped = 0
        repeat(40) {
            val orderId = newId()
            registerShipmentUseCase.register(RegisterShipmentCommand(newId(), orderId))

            val (ship, cancel) = race(
                { shipOrderShipmentUseCase.ship(ShipOrderShipmentCommand(orderId)) },
                { cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId)) },
            )

            assertTrue(ship.isSuccess != cancel.isSuccess, "출고=${ship.exceptionOrNull()}, 취소=${cancel.exceptionOrNull()}")
            if (ship.isSuccess) {
                assertTrue(cancel.exceptionOrNull() is ShipmentCancelRejectedException, "예상하지 못한 실패: ${cancel.exceptionOrNull()}")
                assertEquals(ShipmentStatus.SHIPPED, status(orderId))
                shipped++
            } else {
                assertEquals(ShipmentStatus.CANCELLED, status(orderId))
            }
        }
        println("출고 $shipped / 취소 ${40 - shipped}")
    }

    @Test
    fun `배송 등록과 취소가 동시에 와도 취소된 주문의 배송은 살아 있지 않다`() {
        repeat(40) {
            val orderId = newId()

            val results = race(
                { registerShipmentUseCase.register(RegisterShipmentCommand(newId(), orderId)) },
                { cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId)) },
            )

            results.forEach { it.getOrThrow() }
            assertEquals(ShipmentStatus.CANCELLED, status(orderId))
        }
    }

    @Test
    fun `같은 주문의 취소가 동시에 여러 번 와도 교착 없이 모두 성공한다`() {
        val registered = newId().also { registerShipmentUseCase.register(RegisterShipmentCommand(newId(), it)) }
        val absent = newId()

        listOf(registered, absent).forEach { orderId ->
            val results = race(*Array(8) { { cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId)) } })

            results.forEach { it.getOrThrow() }
            assertEquals(ShipmentStatus.CANCELLED, status(orderId))
        }
    }

    @Test
    fun `배송 등록 하나와 취소 여럿이 겹쳐도 교착 없이 취소로 끝난다`() {
        repeat(20) {
            val orderId = newId()

            val results = race(
                { registerShipmentUseCase.register(RegisterShipmentCommand(newId(), orderId)) },
                { cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId)) },
                { cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId)) },
                { cancelOrderShipmentUseCase.cancel(CancelOrderShipmentCommand(orderId)) },
            )

            results.forEach { it.getOrThrow() }
            assertEquals(ShipmentStatus.CANCELLED, status(orderId))
        }
    }

    @Test
    fun `같은 주문 확정 이벤트를 동시에 여러 번 받아도 배송은 하나만 등록된다`() {
        val orderId = newId()
        val eventId = newId()

        val results = race(*Array(8) { { registerShipmentUseCase.register(RegisterShipmentCommand(eventId, orderId)) } })

        results.forEach { it.getOrThrow() }
        assertEquals(ShipmentStatus.REGISTERED, status(orderId))
    }
}
