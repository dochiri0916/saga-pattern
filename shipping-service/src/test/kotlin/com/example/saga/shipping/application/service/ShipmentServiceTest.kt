package com.example.saga.shipping.application.service

import com.example.saga.shipping.adapter.out.identifier.UuidV7ShipmentIdGenerator
import com.example.saga.shipping.application.command.CancelOrderShipmentCommand
import com.example.saga.shipping.application.command.RegisterShipmentCommand
import com.example.saga.shipping.application.command.ShipOrderShipmentCommand
import com.example.saga.shipping.application.exception.ShipmentCancelRejectedException
import com.example.saga.shipping.application.exception.ShipmentCancelRejectionReason
import com.example.saga.shipping.application.exception.ShipmentNotFoundException
import com.example.saga.shipping.application.port.out.ShipmentRepository
import com.example.saga.shipping.domain.shipment.Shipment
import com.example.saga.shipping.domain.shipment.exception.InvalidShipmentStatusTransitionException
import com.example.saga.shipping.domain.shipment.vo.OrderReferenceId
import com.example.saga.shipping.domain.shipment.vo.ShipmentStatus
import com.github.f4b6a3.uuid.UuidCreator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShipmentServiceTest {

    private val shipments = InMemoryShipmentRepository()
    private val idGenerator = UuidV7ShipmentIdGenerator()
    private val registerService = RegisterShipmentService(idGenerator, shipments, RecordingAuditTrail())
    private val cancelService = CancelOrderShipmentService(idGenerator, shipments, RecordingAuditTrail())
    private val shipService = ShipOrderShipmentService(shipments, RecordingAuditTrail())

    private val orderId = OrderReferenceId.of(UuidCreator.getTimeOrderedEpoch())

    private fun register() = registerService.register(RegisterShipmentCommand(orderId.value))

    private fun cancel() = cancelService.cancel(CancelOrderShipmentCommand(orderId.value))

    private fun ship() = shipService.ship(ShipOrderShipmentCommand(orderId.value))

    private fun status(): ShipmentStatus? = shipments.findByOrderId(orderId)?.status

    @Test
    fun `확정된 주문의 배송을 등록한다`() {
        register()

        assertEquals(ShipmentStatus.REGISTERED, status())
    }

    @Test
    fun `같은 주문의 배송 등록을 여러 번 요청해도 배송은 하나다`() {
        register()
        register()
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
    private val shipments = LinkedHashMap<OrderReferenceId, Shipment>()

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

    override fun findByOrderId(orderId: OrderReferenceId): Shipment? = shipments[orderId]
    override fun findByOrderIdForUpdate(orderId: OrderReferenceId): Shipment? = shipments[orderId]
}
