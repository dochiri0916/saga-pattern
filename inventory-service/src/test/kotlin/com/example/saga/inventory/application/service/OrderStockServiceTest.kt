package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.exception.OrderStockDecreaseRejectedException
import com.example.saga.inventory.application.port.`in`.command.CleanOrderStockRecordsCommand
import com.example.saga.inventory.application.port.`in`.command.DecreaseOrderStockCommand
import com.example.saga.inventory.application.port.`in`.command.OrderStockItemCommand
import com.example.saga.inventory.application.port.`in`.command.RestoreOrderStockCommand
import com.example.saga.inventory.application.port.out.OrderStockRecordRepository
import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.port.out.StockRevision
import com.example.saga.inventory.application.saga.OrderStockRecord
import com.example.saga.inventory.application.saga.OrderStockStatus
import com.example.saga.inventory.application.saga.StockDecreaseRejectionReason
import com.example.saga.inventory.domain.Stock
import com.example.saga.inventory.domain.vo.OrderId
import com.example.saga.inventory.domain.vo.StockId
import com.example.saga.inventory.domain.vo.StockName
import com.example.saga.inventory.domain.vo.StockPrice
import com.example.saga.inventory.domain.vo.StockQuantity
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderStockServiceTest {

    private var now = Instant.parse("2026-10-10T00:00:00Z")
    private val clock = object : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
    }
    private val stocks = InMemoryStockRepository()
    private val records = InMemoryOrderStockRecordRepository { now }
    private val events = RecordingStockEventPublisher()
    private val decreaseService = DecreaseOrderStockService(records, stocks, events)
    private val restoreService = RestoreOrderStockService(records, stocks, events)
    private val cleanService = CleanOrderStockRecordsService(records, clock)

    private val apple = stocks.register(quantity = 10)
    private val pear = stocks.register(quantity = 1)
    private val orderId = OrderId.of(UuidCreator.getTimeOrderedEpoch())

    private fun items(vararg items: Pair<StockId, Int>) = items.map { OrderStockItemCommand(it.first.value, it.second) }

    private fun decrease(vararg items: Pair<StockId, Int>) = decreaseService.decrease(DecreaseOrderStockCommand(orderId.value, items(*items)))

    private fun restore(vararg items: Pair<StockId, Int>) = restoreService.restore(RestoreOrderStockCommand(orderId.value, items(*items)))

    private fun rejectionOf(block: () -> Unit): StockDecreaseRejectionReason =
        assertFailsWith<OrderStockDecreaseRejectedException> { block() }.reason

    @Test
    fun `주문의 모든 항목을 차감하고 재고마다 변경 이벤트를 낸다`() {
        decrease(apple to 3, pear to 1)

        assertEquals(7, stocks.quantity(apple))
        assertEquals(0, stocks.quantity(pear))
        assertEquals(OrderStockStatus.DECREASED, records.status(orderId))
        assertEquals(setOf(apple, pear), events.changed.toSet())
    }

    @Test
    fun `같은 주문으로 다시 차감을 요청해도 한 번만 차감한다`() {
        decrease(apple to 3)
        decrease(apple to 3)

        assertEquals(7, stocks.quantity(apple))
    }

    @Test
    fun `하나라도 부족하면 아무것도 차감하지 않고 거절을 기록한다`() {
        assertEquals(StockDecreaseRejectionReason.INSUFFICIENT_STOCK, rejectionOf { decrease(apple to 3, pear to 2) })

        assertEquals(10, stocks.quantity(apple))
        assertEquals(1, stocks.quantity(pear))
        assertEquals(OrderStockStatus.REJECTED, records.status(orderId))
        assertEquals(emptyList(), events.changed)
    }

    @Test
    fun `거절했던 주문은 다시 요청해도 차감하지 않는다`() {
        rejectionOf { decrease(pear to 2) }

        assertEquals(StockDecreaseRejectionReason.PREVIOUSLY_REJECTED, rejectionOf { decrease(pear to 1) })
        assertEquals(1, stocks.quantity(pear))
    }

    @Test
    fun `없는 재고나 잘못된 수량이 있으면 거절한다`() {
        val unknown = StockId.of(UuidCreator.getTimeOrderedEpoch())

        assertEquals(StockDecreaseRejectionReason.STOCK_NOT_FOUND, rejectionOf { decrease(apple to 1, unknown to 1) })
        assertEquals(10, stocks.quantity(apple))
    }

    @Test
    fun `차감했던 주문만 되돌리고 여러 번 요청해도 한 번만 되돌린다`() {
        decrease(apple to 3)

        restore(apple to 3)
        restore(apple to 3)

        assertEquals(10, stocks.quantity(apple))
        assertEquals(OrderStockStatus.RESTORED, records.status(orderId))
    }

    @Test
    fun `차감한 적 없는 주문의 복원은 재고를 건드리지 않고 이후의 차감 요청을 막는다`() {
        restore(apple to 3)

        assertEquals(10, stocks.quantity(apple))
        assertEquals(OrderStockStatus.CANCELLED, records.status(orderId))
        assertEquals(StockDecreaseRejectionReason.ORDER_CANCELLED, rejectionOf { decrease(apple to 3) })
        assertEquals(10, stocks.quantity(apple))
    }

    @Test
    fun `되돌린 주문은 다시 차감할 수 없다`() {
        decrease(apple to 3)
        restore(apple to 3)

        assertEquals(StockDecreaseRejectionReason.ORDER_CANCELLED, rejectionOf { decrease(apple to 3) })
        assertEquals(10, stocks.quantity(apple))
    }

    @Test
    fun `거절했던 주문의 복원은 재고를 늘리지 않는다`() {
        rejectionOf { decrease(pear to 2) }

        restore(pear to 2)

        assertEquals(1, stocks.quantity(pear))
        assertEquals(OrderStockStatus.CANCELLED, records.status(orderId))
    }

    @Test
    fun `기록 정리는 결과가 더 바뀌지 않는 기록을 보관 기간 뒤에 지우고 차감된 채로 남은 기록은 더 오래 둔다`() {
        val decreased = OrderId.of(UuidCreator.getTimeOrderedEpoch())
        val restored = OrderId.of(UuidCreator.getTimeOrderedEpoch())
        val recent = OrderId.of(UuidCreator.getTimeOrderedEpoch())
        records.saveIfAbsent(OrderStockRecord(decreased, OrderStockStatus.DECREASED))
        records.saveIfAbsent(OrderStockRecord(restored, OrderStockStatus.RESTORED))
        now = now.plus(Duration.ofDays(9))
        records.saveIfAbsent(OrderStockRecord(recent, OrderStockStatus.CANCELLED))
        val command = CleanOrderStockRecordsCommand(Duration.ofDays(8), Duration.ofDays(30), 1000)

        assertEquals(1, cleanService.clean(command))
        assertEquals(OrderStockStatus.DECREASED, records.status(decreased))
        assertEquals(null, records.status(restored))
        assertEquals(OrderStockStatus.CANCELLED, records.status(recent))

        now = now.plus(Duration.ofDays(22))

        assertEquals(2, cleanService.clean(command), "차감 기록은 30일이 지나서야 지운다. 그 사이 8일이 지난 취소 기록도 함께 지운다")
        assertEquals(null, records.status(decreased))
    }

    @Test
    fun `기록 정리는 한 번에 정해진 건수까지만 지운다`() {
        repeat(3) { records.saveIfAbsent(OrderStockRecord(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderStockStatus.RESTORED)) }
        repeat(3) { records.saveIfAbsent(OrderStockRecord(OrderId.of(UuidCreator.getTimeOrderedEpoch()), OrderStockStatus.DECREASED)) }
        now = now.plus(Duration.ofDays(31))
        val command = CleanOrderStockRecordsCommand(Duration.ofDays(8), Duration.ofDays(30), 4)

        assertEquals(4, cleanService.clean(command))
        assertEquals(2, cleanService.clean(command))
    }
}

private class InMemoryStockRepository : StockRepository {
    private val stocks = HashMap<StockId, Stock>()
    private var revision = 0L

    fun register(quantity: Int): StockId {
        val stock = Stock.register(StockId.of(UuidCreator.getTimeOrderedEpoch()), StockName.of("재고"), StockQuantity.of(quantity), StockPrice.of(1000))
        stocks[stock.id] = stock
        return stock.id
    }

    fun quantity(id: StockId): Int = stocks.getValue(id).quantity.value

    override fun save(stock: Stock): StockRevision {
        stocks[stock.id] = stock
        return StockRevision(++revision)
    }

    override fun findById(id: StockId): Stock? = stocks[id]
    override fun findByIdForUpdate(id: StockId): Stock? = stocks[id]
}

private class InMemoryOrderStockRecordRepository(private val now: () -> Instant) : OrderStockRecordRepository {
    private val records = LinkedHashMap<OrderId, Pair<OrderStockRecord, Instant>>()

    fun status(orderId: OrderId): OrderStockStatus? = records[orderId]?.first?.status

    override fun saveIfAbsent(record: OrderStockRecord): Boolean {
        if (record.orderId in records) return false
        records[record.orderId] = record to now()
        return true
    }

    override fun save(record: OrderStockRecord) {
        check(record.orderId in records)
        records[record.orderId] = record to now()
    }

    override fun saveIfAbsentAndLock(record: OrderStockRecord): OrderStockRecord {
        saveIfAbsent(record)
        return records.getValue(record.orderId).first
    }

    override fun findByOrderId(orderId: OrderId): OrderStockRecord? = records[orderId]?.first

    override fun deleteUpdatedBefore(statuses: List<OrderStockStatus>, before: Instant, limit: Int): Int {
        val targets = records.filterValues { (record, updatedAt) -> record.status in statuses && updatedAt.isBefore(before) }.keys.take(limit)
        targets.forEach { records.remove(it) }
        return targets.size
    }
}

private class RecordingStockEventPublisher : StockEventPublisher {
    val changed = mutableListOf<StockId>()

    override fun publishStockChanged(stock: Stock, revision: StockRevision) {
        changed += stock.id
    }
}
