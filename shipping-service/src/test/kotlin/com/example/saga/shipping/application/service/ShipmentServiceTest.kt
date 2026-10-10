package com.example.saga.shipping.application.service

import com.example.saga.shipping.adapter.out.identifier.UuidV7ShipmentIdGenerator
import com.example.saga.shipping.application.exception.ShipmentCancelRejectedException
import com.example.saga.shipping.application.exception.ShipmentCancelRejectionReason
import com.example.saga.shipping.application.exception.ShipmentNotFoundException
import com.example.saga.shipping.application.port.`in`.command.CancelOrderShipmentCommand
import com.example.saga.shipping.application.port.`in`.command.RegisterShipmentCommand
import com.example.saga.shipping.application.port.`in`.command.ShipOrderShipmentCommand
import com.example.saga.shipping.application.port.out.ProcessedEventRepository
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.Shipment
import com.example.saga.shipping.domain.exception.InvalidShipmentStatusTransitionException
import com.example.saga.shipping.domain.vo.OrderId
import com.example.saga.shipping.domain.vo.ShipmentStatus
import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShipmentServiceTest {

    private val shipments = InMemoryShipmentRepository()
    private val idGenerator = UuidV7ShipmentIdGenerator()
    private val registerService = RegisterShipmentService(InMemoryProcessedEventRepository(), idGenerator, shipments)
    private val cancelService = CancelOrderShipmentService(idGenerator, shipments)
    private val shipService = ShipOrderShipmentService(shipments)

    private val orderId = OrderId.of(UuidCreator.getTimeOrderedEpoch())

    private fun register(eventId: UUID = UUID.randomUUID()) = registerService.register(RegisterShipmentCommand(eventId, orderId.value))

    private fun cancel() = cancelService.cancel(CancelOrderShipmentCommand(orderId.value))

    private fun ship() = shipService.ship(ShipOrderShipmentCommand(orderId.value))

    private fun status(): ShipmentStatus? = shipments.findByOrderId(orderId)?.status

    @Test
    fun `확정된 주문의 배송을 등록한다`() {
        register()

        assertEquals(ShipmentStatus.REGISTERED, status())
    }

    @Test
    fun `같은 이벤트가 다시 오거나 같은 주문의 이벤트가 다른 ID로 와도 배송은 하나다`() {
        val eventId = UUID.randomUUID()
        register(eventId)
        register(eventId)
        register()

        assertEquals(1, shipments.count())
    }

    @Test
    fun `등록된 배송은 취소할 수 있고 여러 번 취소해도 된다`() {
        register()

        cancel()
        cancel()

        assertEquals(ShipmentStatus.CANCELLED, status())
    }

    @Test
    fun `출고한 배송은 취소를 거절하고 그대로 둔다`() {
        register()
        ship()

        val rejected = assertFailsWith<ShipmentCancelRejectedException> { cancel() }

        assertEquals(ShipmentCancelRejectionReason.ALREADY_SHIPPED, rejected.reason)
        assertEquals(ShipmentStatus.SHIPPED, status())
    }

    @Test
    fun `배송 등록 전에 취소된 주문은 이벤트가 늦게 와도 등록되지 않는다`() {
        cancel()

        register()

        assertEquals(ShipmentStatus.CANCELLED, status())
        assertEquals(1, shipments.count())
    }

    @Test
    fun `취소된 배송과 없는 배송은 출고할 수 없다`() {
        assertFailsWith<ShipmentNotFoundException> { ship() }

        register()
        cancel()
        assertFailsWith<InvalidShipmentStatusTransitionException> { ship() }
    }
}

private class InMemoryShipmentRepository : ShipmentRepository {
    private val shipments = LinkedHashMap<OrderId, Shipment>()

    fun count(): Int = shipments.size

    override fun save(shipment: Shipment): Shipment = shipment.also { shipments[it.orderId] = it }

    override fun saveIfAbsent(shipment: Shipment): Boolean {
        if (shipment.orderId in shipments) return false
        shipments[shipment.orderId] = shipment
        return true
    }

    override fun saveIfAbsentAndLock(shipment: Shipment): Shipment {
        saveIfAbsent(shipment)
        return shipments.getValue(shipment.orderId)
    }

    override fun findByOrderId(orderId: OrderId): Shipment? = shipments[orderId]
    override fun findByOrderIdForUpdate(orderId: OrderId): Shipment? = shipments[orderId]
}

private class InMemoryProcessedEventRepository : ProcessedEventRepository {
    private val processed = HashSet<UUID>()

    override fun saveIfAbsent(eventId: UUID): Boolean = processed.add(eventId)
}
