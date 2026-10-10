package com.example.saga.order.application.service

import com.example.saga.order.application.port.out.InventoryClient
import com.example.saga.order.application.port.out.OrderEventPublisher
import com.example.saga.order.application.port.out.OrderIdempotencyKey
import com.example.saga.order.application.port.out.OrderIdempotencyKeyRepository
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.port.out.PaymentClient
import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.application.saga.OrderSaga
import com.example.saga.order.domain.Order
import com.example.saga.order.domain.event.OrderCompleted
import com.example.saga.order.domain.vo.OrderId
import com.example.saga.order.domain.vo.StockReferenceId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

internal class MutableClock(private var now: Instant) : Clock() {
    fun advance(duration: Duration) {
        now = now.plus(duration)
    }

    override fun instant(): Instant = now
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = this
}

internal class InMemoryOrderSagaRepository : OrderSagaRepository {
    val sagas = LinkedHashMap<OrderId, OrderSaga>()

    override fun save(saga: OrderSaga): OrderSaga = saga.also { sagas[it.orderId] = it }
    override fun findByOrderId(orderId: OrderId): OrderSaga? = sagas[orderId]
    override fun findByOrderIdForUpdate(orderId: OrderId): OrderSaga? = sagas[orderId]
    override fun findStalledForUpdate(before: Instant, limit: Int): List<OrderSaga> =
        sagas.values.filter { it.status.waiting && it.updatedAt.isBefore(before) }.sortedBy { it.updatedAt }.take(limit)

    override fun countUnfinishedStartedBefore(before: Instant): Long =
        sagas.values.count { it.status.waiting && it.startedAt.isBefore(before) }.toLong()
}

internal class InMemoryOrderRepository : OrderRepository {
    val orders = HashMap<OrderId, Order>()

    override fun save(order: Order): Order = order.also { orders[it.id] = it }
    override fun findById(id: OrderId): Order? = orders[id]
}

internal class RecordingOrderEventPublisher : OrderEventPublisher {
    val completed = mutableListOf<OrderId>()

    override fun publish(order: Order) {
        if (order.events.any { it is OrderCompleted }) completed += order.id
    }
}

internal class FakeParticipants : InventoryClient, PaymentClient, ShippingClient {
    val calls = mutableListOf<String>()
    var decrease: () -> String? = { null }
    var restore: () -> Unit = { }
    var pay: () -> String? = { null }
    var cancelPayment: () -> Unit = { }
    var cancelShipment: () -> String? = { null }

    override fun decreaseStock(order: Order): String? = record("decrease").let { decrease() }
    override fun restoreStock(order: Order) = record("restore").let { restore() }
    override fun pay(order: Order): String? = record("pay").let { pay() }
    override fun cancelPayment(order: Order) = record("cancelPayment").let { cancelPayment() }
    override fun cancelShipment(order: Order): String? = record("cancelShipment").let { cancelShipment() }

    private fun record(call: String) {
        calls += call
    }
}

internal class InMemoryOrderIdempotencyKeyRepository : OrderIdempotencyKeyRepository {
    val keys = HashMap<String, Pair<OrderIdempotencyKey, Instant>>()
    var now: () -> Instant = { Instant.EPOCH }

    override fun saveIfAbsent(idempotencyKey: OrderIdempotencyKey): Boolean =
        keys.putIfAbsent(idempotencyKey.key, idempotencyKey to now()) == null

    override fun findByKey(key: String): OrderIdempotencyKey? = keys[key]?.first

    override fun deleteCreatedBefore(before: Instant, limit: Int): Int {
        val targets = keys.filterValues { it.second.isBefore(before) }.keys.take(limit)
        targets.forEach { keys.remove(it) }
        return targets.size
    }
}

internal class InMemoryStockSnapshotRepository : StockSnapshotRepository {
    val snapshots = HashMap<StockReferenceId, StockSnapshot>()

    override fun saveIfNewer(stockSnapshot: StockSnapshot): Boolean = true.also { snapshots[stockSnapshot.stockReferenceId] = stockSnapshot }
    override fun findById(stockReferenceId: StockReferenceId): StockSnapshot? = snapshots[stockReferenceId]
}
