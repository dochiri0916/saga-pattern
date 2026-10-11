package com.example.saga.order

import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.port.out.OrderSagaRepository
import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.application.saga.model.OrderSagaReply
import com.example.saga.order.application.saga.model.OrderSagaStatus
import com.example.saga.order.application.saga.orchestrator.OrderSagaStepExecutor
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.OrderItem
import com.example.saga.order.domain.order.OrderItems
import com.example.saga.order.domain.order.vo.OrderId
import com.example.saga.order.domain.order.vo.OrderItemId
import com.example.saga.order.domain.order.vo.OrderQuantity
import com.example.saga.order.domain.order.vo.OrderStatus
import com.example.saga.order.domain.order.vo.StockReferenceId
import com.example.saga.order.domain.order.vo.UnitPrice
import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest(classes = [OrderIntegrationTestConfig::class], properties = [MYSQL_URL, CREATE_TABLES, POOL_SIZE, NO_OUTBOX, NO_CONSUMER])
class OrderSagaLockingIntegrationTest @Autowired constructor(
    private val placementHandler: com.example.saga.order.adapter.`in`.web.OrderPlacementHandler,
    private val executor: OrderSagaStepExecutor,
    private val orderRepository: OrderRepository,
    private val orderSagaRepository: OrderSagaRepository,
    private val stockSnapshotRepository: StockSnapshotRepository,
    private val participants: StubParticipants,
    private val transactionTemplate: TransactionTemplate,
) {

    private val stock = StockReferenceId.of(UuidCreator.getTimeOrderedEpoch())

    private fun newOrder(): Order = Order.register(
        OrderId.of(UuidCreator.getTimeOrderedEpoch()),
        OrderItems.of(listOf(OrderItem.register(OrderItemId.of(UuidCreator.getTimeOrderedEpoch()), stock, OrderQuantity.of(1), UnitPrice.of(1000)))),
    )

    @Test
    fun `같은 멱등 키의 주문 요청이 동시에 와도 주문은 하나만 만들어지고 한 번만 진행된다`() {
        transactionTemplate.executeWithoutResult { stockSnapshotRepository.saveIfNewer(StockSnapshot(stock, "sample", 1000, 10, 1)) }
        val key = "key-${UuidCreator.getTimeOrderedEpoch()}"
        val request = com.example.saga.order.adapter.`in`.web.request.PlaceOrderRequest(listOf(com.example.saga.order.adapter.`in`.web.request.PlaceOrderItemRequest(stock.value, 1)))

        val results = race(*Array(8) { { placementHandler.place(key, request) } })

        val orderIds = results.map { it.getOrThrow().orderId }.toSet()
        assertEquals(1, orderIds.size)
        val orderId = OrderId.of(java.util.UUID.fromString(orderIds.single()))
        assertEquals(1, participants.decreaseCount(orderId))
        assertEquals(OrderStatus.COMPLETED, orderRepository.findById(orderId)?.status)
    }

    @Test
    fun `같은 호출 결과를 동시에 반영해도 전이는 한 번만 일어난다`() {
        val order = newOrder()
        executor.start(order)

        val results = race(*Array(8) { { executor.apply(order.id, OrderSagaStatus.STOCK_REQUESTED, OrderSagaReply.STOCK_DECREASED, null) } })

        assertEquals(1, results.count { it.getOrThrow() != null })
        assertEquals(OrderSagaStatus.PAYMENT_REQUESTED, orderSagaRepository.findByOrderId(order.id)?.status)
    }

    @Test
    fun `결제 승인 반영과 취소 요청이 동시에 와도 한쪽 순서로만 정해진다`() {
        repeat(20) {
            val order = newOrder()
            executor.start(order)
            executor.apply(order.id, OrderSagaStatus.STOCK_REQUESTED, OrderSagaReply.STOCK_DECREASED, null)

            val (approval, cancel) = race(
                { executor.apply(order.id, OrderSagaStatus.PAYMENT_REQUESTED, OrderSagaReply.PAYMENT_APPROVED, null) },
                { executor.cancel(order.id) },
            )

            cancel.getOrThrow()
            val saga = assertNotNull(orderSagaRepository.findByOrderId(order.id))
            val orderStatus = orderRepository.findById(order.id)?.status
            if (approval.getOrThrow() != null) {

                assertEquals(OrderSagaStatus.SHIPMENT_CANCEL_REQUESTED, saga.status)
                assertEquals(OrderStatus.COMPLETED, orderStatus)
            } else {

                assertEquals(OrderSagaStatus.PAYMENT_CANCEL_REQUESTED, saga.status)
                assertEquals(OrderStatus.PENDING, orderStatus)
            }
        }
    }

    @Test
    fun `복구 작업이 동시에 돌아도 같은 사가를 겹쳐 집지 않는다`() {
        val orders = List(30) { newOrder().also { executor.start(it) } }
        Thread.sleep(50)
        val before = Instant.now()

        val claimed = race(
            *Array(3) {
                {
                    val mine = mutableListOf<OrderId>()
                    do {
                        val batch = executor.claimStalled(before, 7).map { it.orderId }
                        mine += batch
                    } while (batch.isNotEmpty())
                    mine
                }
            }
        ).flatMap { it.getOrThrow() }.filter { id -> orders.any { it.id == id } }

        assertEquals(30, claimed.size, "멈춘 사가를 빠짐없이, 한 번씩만 집어야 한다")
        assertEquals(30, claimed.toSet().size)
        assertTrue(orders.all { orderSagaRepository.findByOrderId(it.id)?.status == OrderSagaStatus.STOCK_RESTORE_REQUESTED })
    }
}
