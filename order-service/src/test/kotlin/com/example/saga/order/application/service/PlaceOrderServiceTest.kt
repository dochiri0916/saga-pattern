package com.example.saga.order.application.service

import com.example.saga.order.adapter.out.identifier.UuidV7OrderIdGenerator
import com.example.saga.order.adapter.out.identifier.UuidV7OrderItemIdGenerator
import com.example.saga.order.application.exception.IdempotencyKeyReusedException
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.port.`in`.command.CleanOrderIdempotencyKeysCommand
import com.example.saga.order.application.port.`in`.command.PlaceOrderCommand
import com.example.saga.order.application.port.`in`.command.PlaceOrderItemCommand
import com.example.saga.order.application.port.out.ParticipantCallException
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.application.saga.OrderCancelPolicy
import com.example.saga.order.domain.vo.OrderStatus
import com.example.saga.order.domain.vo.StockReferenceId
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class PlaceOrderServiceTest {

    private val clock = MutableClock(Instant.parse("2026-10-10T00:00:00Z"))
    private val sagas = InMemoryOrderSagaRepository()
    private val orders = InMemoryOrderRepository()
    private val keys = InMemoryOrderIdempotencyKeyRepository().apply { now = { clock.instant() } }
    private val snapshots = InMemoryStockSnapshotRepository()
    private val participants = FakeParticipants()
    private val executor = OrderSagaStepExecutor(sagas, orders, keys, RecordingOrderEventPublisher(), OrderCancelPolicy(Duration.ofDays(7)), clock)
    private val service =
        PlaceOrderService(UuidV7OrderIdGenerator(), UuidV7OrderItemIdGenerator(), orders, sagas, keys, snapshots, executor, OrderSagaDriver(executor, participants, participants, participants))

    private val apple = register()
    private val pear = register()

    private fun register(): StockReferenceId = StockReferenceId.of(UuidCreator.getTimeOrderedEpoch())
        .also { snapshots.snapshots[it] = StockSnapshot(it, "sample", 1000, 10, 1) }

    private fun command(key: String?, vararg items: Pair<StockReferenceId, Int>) =
        PlaceOrderCommand(items.map { PlaceOrderItemCommand(it.first.value, it.second) }, key)

    @Test
    fun `같은 키로 다시 요청하면 주문을 새로 만들지 않고 처음 주문으로 답한다`() {
        val first = service.place(command("key-1", apple to 2, pear to 1))
        val second = service.place(command("key-1", pear to 1, apple to 2))

        assertEquals(first, second)
        assertEquals(OrderStatus.COMPLETED.name, second.status)
        assertEquals(1, orders.orders.size)
        assertEquals(listOf("decrease", "pay"), participants.calls)
    }

    @Test
    fun `키가 없거나 다르면 요청마다 주문을 만든다`() {
        val first = service.place(command(null, apple to 2))
        val second = service.place(command(null, apple to 2))
        val third = service.place(command("key-1", apple to 2))
        val fourth = service.place(command("key-2", apple to 2))

        assertEquals(4, setOf(first.orderId, second.orderId, third.orderId, fourth.orderId).size)
    }

    @Test
    fun `같은 키로 다른 내용을 보내면 주문을 만들지 않고 거절한다`() {
        service.place(command("key-1", apple to 2))

        assertFailsWith<IdempotencyKeyReusedException> { service.place(command("key-1", apple to 3)) }
        assertEquals(1, orders.orders.size)
    }

    @Test
    fun `거절된 주문의 키로 다시 요청하면 다시 시도하지 않고 같은 거절로 답한다`() {
        participants.decrease = { "INSUFFICIENT_STOCK" }
        val first = assertFailsWith<OrderRejectedException> { service.place(command("key-1", apple to 2)) }
        participants.decrease = { null }

        val second = assertFailsWith<OrderRejectedException> { service.place(command("key-1", apple to 2)) }

        assertEquals(first.orderId, second.orderId)
        assertEquals("INSUFFICIENT_STOCK", second.reason)
        assertEquals(listOf("decrease"), participants.calls)
    }

    @Test
    fun `처음 요청이 아직 되돌리는 중이면 진행시키지 않고 PENDING으로 답한다`() {
        participants.pay = { "GATEWAY_DECLINED" }
        participants.restore = { throw ParticipantCallException("응답 없음") }
        val first = service.place(command("key-1", apple to 2))
        participants.calls.clear()

        val second = service.place(command("key-1", apple to 2))

        assertEquals(first.orderId, second.orderId)
        assertEquals(OrderStatus.PENDING.name, second.status)
        assertEquals(emptyList(), participants.calls)
    }

    @Test
    fun `보관 기간이 지나 지워진 키로 요청하면 새 주문이 된다`() {
        val first = service.place(command("key-1", apple to 2))
        clock.advance(Duration.ofHours(25))
        service.place(command("key-2", apple to 2))

        val deleted = CleanOrderIdempotencyKeysService(keys, clock).clean(CleanOrderIdempotencyKeysCommand(Duration.ofHours(24), 1000))
        val again = service.place(command("key-1", apple to 2))

        assertEquals(1, deleted)
        assertNotEquals(first.orderId, again.orderId)
        assertEquals(3, orders.orders.size)
    }
}
