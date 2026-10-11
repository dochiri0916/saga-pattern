package com.example.saga.order.adapter.`in`.web

import com.example.saga.infrastructure.idempotency.IdempotencyInterceptor
import com.example.saga.infrastructure.idempotency.IdempotencyJpaEntity
import com.example.saga.infrastructure.idempotency.IdempotencyKeyReusedException
import com.example.saga.infrastructure.idempotency.IdempotencyManager
import com.example.saga.infrastructure.idempotency.IdempotencyRepository
import com.example.saga.order.adapter.`in`.web.request.PlaceOrderItemRequest
import com.example.saga.order.adapter.`in`.web.request.PlaceOrderRequest
import com.example.saga.order.adapter.out.identifier.UuidV7OrderIdGenerator
import com.example.saga.order.adapter.out.identifier.UuidV7OrderItemIdGenerator
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.port.out.ParticipantCallException
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.application.saga.orchestrator.OrderSagaDriver
import com.example.saga.order.application.saga.orchestrator.OrderSagaStepExecutor
import com.example.saga.order.application.service.FakeParticipants
import com.example.saga.order.application.service.InMemoryOrderRepository
import com.example.saga.order.application.service.InMemoryOrderSagaRepository
import com.example.saga.order.application.service.InMemoryStockSnapshotRepository
import com.example.saga.order.application.service.MutableClock
import com.example.saga.order.application.service.PlaceOrderService
import com.example.saga.order.application.service.RecordingAuditTrail
import com.example.saga.order.application.service.RecordingEventPublicationPort
import com.example.saga.order.application.service.StartOrderService
import com.example.saga.order.domain.order.policy.OrderCancelPolicy
import com.example.saga.order.domain.order.vo.OrderStatus
import com.example.saga.order.domain.order.vo.StockReferenceId
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class OrderPlacementHandlerTest {

    private val clock = MutableClock(Instant.parse("2026-10-10T00:00:00Z"))
    private val sagas = InMemoryOrderSagaRepository()
    private val orders = InMemoryOrderRepository()
    private val keys = InMemoryHttpKeys(clock)
    private val snapshots = InMemoryStockSnapshotRepository()
    private val participants = FakeParticipants()
    private val executor = OrderSagaStepExecutor(sagas, orders, RecordingEventPublicationPort(), OrderCancelPolicy(Duration.ofDays(7)), clock, RecordingAuditTrail())
    private val service =
        PlaceOrderService(StartOrderService(UuidV7OrderIdGenerator(), UuidV7OrderItemIdGenerator(), snapshots, executor), orders, sagas, OrderSagaDriver(executor, participants, participants, participants))

    private val handler = OrderPlacementHandler(StartOrderService(UuidV7OrderIdGenerator(), UuidV7OrderItemIdGenerator(), snapshots, executor), service, service, IdempotencyInterceptor(IdempotencyManager(keys.repository, clock)))

    private val apple = register()
    private val pear = register()

    private fun register(): StockReferenceId = StockReferenceId.of(UuidCreator.getTimeOrderedEpoch())
        .also { snapshots.snapshots[it] = StockSnapshot(it, "sample", 1000, 10, 1) }

    private fun command(key: String?, vararg items: Pair<StockReferenceId, Int>) =
        key to PlaceOrderRequest(items.map { PlaceOrderItemRequest(it.first.value, it.second) })

    private fun place(command: Pair<String?, PlaceOrderRequest>) = handler.place(command.first, command.second)

    @Test
    fun `같은 키로 다시 요청하면 주문을 새로 만들지 않고 처음 주문으로 답한다`() {
        val first = place(command("key-1", apple to 2, pear to 1))
        val second = place(command("key-1", pear to 1, apple to 2))

        assertEquals(first, second)
        assertEquals(OrderStatus.COMPLETED.name, second.status)
        assertEquals(1, orders.orders.size)
        assertEquals(listOf("decrease", "pay"), participants.calls)
    }

    @Test
    fun `키가 없거나 다르면 요청마다 주문을 만든다`() {
        val first = place(command(null, apple to 2))
        val second = place(command(null, apple to 2))
        val third = place(command("key-1", apple to 2))
        val fourth = place(command("key-2", apple to 2))

        assertEquals(4, setOf(first.orderId, second.orderId, third.orderId, fourth.orderId).size)
    }

    @Test
    fun `같은 키로 다른 내용을 보내면 주문을 만들지 않고 거절한다`() {
        place(command("key-1", apple to 2))

        assertFailsWith<IdempotencyKeyReusedException> { place(command("key-1", apple to 3)) }
        assertEquals(1, orders.orders.size)
    }

    @Test
    fun `거절된 주문의 키로 다시 요청하면 다시 시도하지 않고 같은 거절로 답한다`() {
        participants.decrease = { "INSUFFICIENT_STOCK" }
        val first = assertFailsWith<OrderRejectedException> { place(command("key-1", apple to 2)) }
        participants.decrease = { null }

        val second = assertFailsWith<OrderRejectedException> { place(command("key-1", apple to 2)) }

        assertEquals(first.orderId, second.orderId)
        assertEquals("INSUFFICIENT_STOCK", second.reason)
        assertEquals(listOf("decrease"), participants.calls)
    }

    @Test
    fun `처음 요청이 아직 되돌리는 중이면 진행시키지 않고 PENDING으로 답한다`() {
        participants.pay = { "GATEWAY_DECLINED" }
        participants.restore = { throw ParticipantCallException("응답 없음") }
        val first = place(command("key-1", apple to 2))
        participants.calls.clear()

        val second = place(command("key-1", apple to 2))

        assertEquals(first.orderId, second.orderId)
        assertEquals(OrderStatus.PENDING.name, second.status)
        assertEquals(emptyList(), participants.calls)
    }

    @Test
    fun `보관 기간이 지나 지워진 키로 요청하면 새 주문이 된다`() {
        val first = place(command("key-1", apple to 2))
        clock.advance(Duration.ofHours(25))
        place(command("key-2", apple to 2))

        val deleted = keys.repository.deleteCreatedBefore(clock.instant().minus(Duration.ofHours(24)), 1000)
        val again = place(command("key-1", apple to 2))

        assertEquals(1, deleted)
        assertNotEquals(first.orderId, again.orderId)
        assertEquals(3, orders.orders.size)
    }
}

private class InMemoryHttpKeys(private val clock: java.time.Clock) {
    private val entries = HashMap<String, IdempotencyJpaEntity>()
    val repository = java.lang.reflect.Proxy.newProxyInstance(
        IdempotencyRepository::class.java.classLoader, arrayOf(IdempotencyRepository::class.java),
    ) { _, method, args ->
        when (method.name) {
            "insertOrLock" -> {
                val id = args[0] as String
                if (id in entries) 0 else {
                    entries[id] = IdempotencyJpaEntity.of(id, args[1] as String, args[2] as String, args[3] as String, null, clock.instant())
                    1
                }
            }
            "attach" -> {
                val id = args[0] as String
                val entry = entries.getValue(id)
                entries[id] = IdempotencyJpaEntity.of(id, entry.scope, entry.key, entry.requestHash, args[1] as String, entry.createdAt)
                1
            }
            "findForUpdate" -> entries[args[0] as String]
            "deleteCreatedBefore" -> {
                val before = args[0] as Instant
                val targets = entries.filterValues { it.createdAt.isBefore(before) }.keys.take(args[1] as Int)
                targets.forEach(entries::remove)
                targets.size
            }
            else -> error("지원하지 않는 테스트 Repository 호출: ${method.name}")
        }
    } as IdempotencyRepository
}
