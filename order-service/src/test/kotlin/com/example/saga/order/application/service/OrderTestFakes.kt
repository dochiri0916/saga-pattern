package com.example.saga.order.application.service

import com.example.saga.order.application.port.out.EventPublicationPort
import com.example.saga.order.application.port.out.InventoryClient
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.port.out.PaymentClient
import com.example.saga.order.application.port.out.ShippingClient
import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.application.saga.model.OrderSaga
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.StockReferenceId
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

internal class RecordingEventPublicationPort : EventPublicationPort {
    val completed = mutableListOf<OrderId>()

    override fun publish(event: com.example.saga.order.contract.event.OrderCompletedEvent) {
        completed += OrderId.of(java.util.UUID.fromString(event.orderId))
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

internal class InMemoryStockSnapshotRepository : StockSnapshotRepository {
    val snapshots = HashMap<StockReferenceId, StockSnapshot>()

    override fun saveIfNewer(stockSnapshot: StockSnapshot): Boolean = true.also { snapshots[stockSnapshot.stockReferenceId] = stockSnapshot }
    override fun findById(stockReferenceId: StockReferenceId): StockSnapshot? = snapshots[stockReferenceId]
}
