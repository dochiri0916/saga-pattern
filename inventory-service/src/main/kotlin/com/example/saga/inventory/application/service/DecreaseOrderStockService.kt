package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.exception.OrderStockDecreaseRejectedException
import com.example.saga.inventory.application.port.`in`.command.DecreaseOrderStockCommand
import com.example.saga.inventory.application.port.`in`.command.OrderStockItemCommand
import com.example.saga.inventory.application.port.`in`.usecase.DecreaseOrderStockUseCase
import com.example.saga.inventory.application.port.out.OrderStockRecordRepository
import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.saga.OrderStockRecord
import com.example.saga.inventory.application.saga.OrderStockStatus
import com.example.saga.inventory.application.saga.StockDecreaseRejectionReason
import com.example.saga.inventory.domain.Stock
import com.example.saga.inventory.domain.exception.InsufficientStockException
import com.example.saga.inventory.domain.exception.InvalidStockAmountException
import com.example.saga.inventory.domain.vo.OrderId
import com.example.saga.inventory.domain.vo.StockId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(DecreaseOrderStockService::class.java)

@Service
class DecreaseOrderStockService(
    private val orderStockRecordRepository: OrderStockRecordRepository,
    private val stockRepository: StockRepository,
    private val stockEventPublisher: StockEventPublisher,
) : DecreaseOrderStockUseCase {

    @Transactional(noRollbackFor = [OrderStockDecreaseRejectedException::class])
    override fun decrease(command: DecreaseOrderStockCommand) {
        val orderId = OrderId.of(command.orderId)

        if (!orderStockRecordRepository.saveIfAbsent(OrderStockRecord(orderId, OrderStockStatus.DECREASED))) {
            answerAsRecorded(orderId)
            return
        }

        val rejection = decreaseAll(command.items) ?: return
        orderStockRecordRepository.save(OrderStockRecord(orderId, OrderStockStatus.REJECTED))
        log.info("주문의 재고 차감을 거절했습니다. orderId={}, reason={}, stockId={}", orderId, rejection.reason, rejection.stockId)
        throw OrderStockDecreaseRejectedException(command.orderId.toString(), rejection.reason, rejection.stockId?.toString())
    }

    private fun answerAsRecorded(orderId: OrderId) {

        val record = checkNotNull(orderStockRecordRepository.findByOrderId(orderId)) {
            "주문별 재고 기록이 사라졌습니다. orderId=$orderId"
        }
        log.info("이미 결과가 정해진 주문이라서 차감하지 않습니다. orderId={}, status={}", orderId, record.status)
        when (record.status) {
            OrderStockStatus.DECREASED -> Unit
            OrderStockStatus.REJECTED ->
                throw OrderStockDecreaseRejectedException(orderId.toString(), StockDecreaseRejectionReason.PREVIOUSLY_REJECTED, null)
            OrderStockStatus.RESTORED, OrderStockStatus.CANCELLED ->
                throw OrderStockDecreaseRejectedException(orderId.toString(), StockDecreaseRejectionReason.ORDER_CANCELLED, null)
        }
    }

    private fun decreaseAll(items: List<OrderStockItemCommand>): Rejection? {
        if (items.isEmpty()) return Rejection(StockDecreaseRejectionReason.INVALID_ORDER_ITEM)
        val requests = items.map { StockId.of(it.stockId) to it.quantity }

        val stocks = LinkedHashMap<StockId, Stock>()
        for (stockId in requests.map { it.first }.distinct().sortedBy { it.value }) {
            stocks[stockId] = stockRepository.findByIdForUpdate(stockId)
                ?: return Rejection(StockDecreaseRejectionReason.STOCK_NOT_FOUND, stockId)
        }

        for ((stockId, quantity) in requests) {
            stocks[stockId] = try {
                stocks.getValue(stockId).decrease(quantity)
            } catch (e: InsufficientStockException) {
                return Rejection(StockDecreaseRejectionReason.INSUFFICIENT_STOCK, stockId)
            } catch (e: InvalidStockAmountException) {
                return Rejection(StockDecreaseRejectionReason.INVALID_ORDER_ITEM, stockId)
            }
        }

        stocks.values.forEach { stockEventPublisher.publishStockChanged(it, stockRepository.save(it)) }
        return null
    }

    private data class Rejection(val reason: StockDecreaseRejectionReason, val stockId: StockId? = null)
}
