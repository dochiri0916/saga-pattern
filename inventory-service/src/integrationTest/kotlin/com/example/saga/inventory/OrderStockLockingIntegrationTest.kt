package com.example.saga.inventory

import com.example.saga.inventory.adapter.out.persistence.repository.OrderStockRecordJpaRepository
import com.example.saga.inventory.application.command.DecreaseOrderStockCommand
import com.example.saga.inventory.application.command.OrderStockItemCommand
import com.example.saga.inventory.application.command.RegisterStockCommand
import com.example.saga.inventory.application.command.RestoreOrderStockCommand
import com.example.saga.inventory.application.exception.OrderStockDecreaseRejectedException
import com.example.saga.inventory.application.port.`in`.DecreaseOrderStockUseCase
import com.example.saga.inventory.application.port.`in`.RegisterStockUseCase
import com.example.saga.inventory.application.port.`in`.RestoreOrderStockUseCase
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.domain.orderstock.vo.OrderStockStatus
import com.example.saga.inventory.domain.orderstock.vo.StockDecreaseRejectionReason
import com.example.saga.inventory.domain.stock.vo.StockId
import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest(classes = [InventoryIntegrationTestConfig::class], properties = [MYSQL_URL, CREATE_TABLES, POOL_SIZE, NO_OUTBOX, NO_CONSUMER])
class OrderStockLockingIntegrationTest @Autowired constructor(
    private val registerStockUseCase: RegisterStockUseCase,
    private val decreaseOrderStockUseCase: DecreaseOrderStockUseCase,
    private val restoreOrderStockUseCase: RestoreOrderStockUseCase,
    private val stockRepository: StockRepository,
    private val orderStockRecordJpaRepository: OrderStockRecordJpaRepository,
) {

    private fun register(quantity: Int): UUID = UUID.fromString(registerStockUseCase.register(RegisterStockCommand("sample", quantity, 1000)).stockId)

    private fun quantity(stockId: UUID): Int? = stockRepository.findById(StockId.of(stockId))?.quantity?.value

    private fun recordStatus(orderId: UUID): String? = orderStockRecordJpaRepository.findByIdOrNull(orderId)?.status

    private fun decrease(orderId: UUID, vararg items: Pair<UUID, Int>) =
        decreaseOrderStockUseCase.decrease(DecreaseOrderStockCommand(orderId, items.map { OrderStockItemCommand(it.first, it.second) }))

    private fun restore(orderId: UUID, vararg items: Pair<UUID, Int>) =
        restoreOrderStockUseCase.restore(RestoreOrderStockCommand(orderId, items.map { OrderStockItemCommand(it.first, it.second) }))

    private fun newOrderId(): UUID = UuidCreator.getTimeOrderedEpoch()

    @Test
    fun `차감을 거절하면 예외를 던지면서도 거절 기록은 커밋되고 재고는 그대로다`() {
        val stock = register(1)
        val orderId = newOrderId()

        val rejected = assertFailsWith<OrderStockDecreaseRejectedException> { decrease(orderId, stock to 2) }

        assertEquals(StockDecreaseRejectionReason.INSUFFICIENT_STOCK, rejected.reason)
        assertEquals(OrderStockStatus.REJECTED.name, recordStatus(orderId))
        assertEquals(1, quantity(stock))
    }

    @Test
    fun `여러 주문이 같은 재고를 동시에 차감해도 가진 만큼만 팔린다`() {
        val stock = register(10)
        val orderIds = List(20) { newOrderId() }

        val results = race(*orderIds.map { orderId -> { decrease(orderId, stock to 1) } }.toTypedArray())

        results.forEach { result -> result.exceptionOrNull()?.let { assertTrue(it is OrderStockDecreaseRejectedException, "예상하지 못한 실패: $it") } }
        assertEquals(10, results.count { it.isSuccess })
        assertEquals(0, quantity(stock))
        assertEquals(10, orderIds.count { recordStatus(it) == OrderStockStatus.DECREASED.name })
    }

    @Test
    fun `같은 주문의 차감 요청이 동시에 와도 한 번만 차감한다`() {
        val stock = register(10)
        val orderId = newOrderId()

        val results = race(*Array(8) { { decrease(orderId, stock to 1) } })

        results.forEach { it.getOrThrow() }
        assertEquals(9, quantity(stock))
    }

    @Test
    fun `같은 주문의 복원 요청이 동시에 여러 번 와도 교착 없이 한 번만 되돌린다`() {
        val stock = register(10)
        val orderId = newOrderId()
        decrease(orderId, stock to 3)

        val results = race(*Array(8) { { restore(orderId, stock to 3) } })

        results.forEach { it.getOrThrow() }
        assertEquals(10, quantity(stock))
        assertEquals(OrderStockStatus.RESTORED.name, recordStatus(orderId))
    }

    @Test
    fun `같은 주문의 차감 요청과 복원 요청이 여럿 겹쳐도 교착 없이 재고가 맞는다`() {
        val stock = register(100)

        repeat(20) {
            val orderId = newOrderId()
            val results = race(
                { decrease(orderId, stock to 1) },
                { decrease(orderId, stock to 1) },
                { decrease(orderId, stock to 1) },
                { restore(orderId, stock to 1) },
                { restore(orderId, stock to 1) },
                { restore(orderId, stock to 1) },
            )

            results.forEach { result ->
                result.exceptionOrNull()?.let { assertTrue(it is OrderStockDecreaseRejectedException, "예상하지 못한 실패: $it") }
            }
            assertTrue(recordStatus(orderId) in listOf(OrderStockStatus.RESTORED.name, OrderStockStatus.CANCELLED.name))
        }

        assertEquals(100, quantity(stock))
    }

    @Test
    fun `같은 주문의 차감과 복원이 동시에 와도 재고가 새거나 늘지 않는다`() {
        val stock = register(100)
        val orderIds = List(40) { newOrderId() }

        orderIds.forEach { orderId ->
            val (decrease, restore) = race({ decrease(orderId, stock to 1) }, { restore(orderId, stock to 1) })

            restore.getOrThrow()
            decrease.exceptionOrNull()?.let { assertTrue(it is OrderStockDecreaseRejectedException, "예상하지 못한 실패: $it") }

            assertTrue(recordStatus(orderId) in listOf(OrderStockStatus.RESTORED.name, OrderStockStatus.CANCELLED.name))
        }

        assertEquals(100, quantity(stock))
    }

    @Test
    fun `여러 재고를 서로 다른 순서로 담은 주문들이 동시에 와도 교착 없이 모두 차감한다`() {
        val apple = register(100)
        val pear = register(100)

        repeat(20) {
            val results = race(
                { decrease(newOrderId(), apple to 1, pear to 1) },
                { decrease(newOrderId(), pear to 1, apple to 1) },
            )
            results.forEach { it.getOrThrow() }
        }

        assertEquals(60, quantity(apple))
        assertEquals(60, quantity(pear))
    }
}
