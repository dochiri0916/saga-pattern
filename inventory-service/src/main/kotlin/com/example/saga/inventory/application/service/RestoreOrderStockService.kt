package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.inventory.application.port.`in`.command.RestoreOrderStockCommand
import com.example.saga.inventory.application.port.`in`.usecase.RestoreOrderStockUseCase
import com.example.saga.inventory.application.port.out.OrderStockRecordRepository
import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.saga.OrderStockRecord
import com.example.saga.inventory.application.saga.OrderStockStatus
import com.example.saga.inventory.domain.Stock
import com.example.saga.inventory.domain.vo.OrderId
import com.example.saga.inventory.domain.vo.StockId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(RestoreOrderStockService::class.java)

@Service
class RestoreOrderStockService(
    private val orderStockRecordRepository: OrderStockRecordRepository,
    private val stockRepository: StockRepository,
    private val stockEventPublisher: StockEventPublisher,
) : RestoreOrderStockUseCase {

    @Transactional
    override fun restore(command: RestoreOrderStockCommand) {
        val orderId = OrderId.of(command.orderId)

        val record = orderStockRecordRepository.saveIfAbsentAndLock(OrderStockRecord(orderId, OrderStockStatus.CANCELLED))
        when (record.status) {
            OrderStockStatus.DECREASED -> {
                restoreAll(command)
                orderStockRecordRepository.save(OrderStockRecord(orderId, OrderStockStatus.RESTORED))
                log.info("주문의 재고를 되돌렸습니다. orderId={}", orderId)
            }
            OrderStockStatus.REJECTED -> orderStockRecordRepository.save(OrderStockRecord(orderId, OrderStockStatus.CANCELLED))

            OrderStockStatus.RESTORED, OrderStockStatus.CANCELLED -> log.info("되돌릴 재고가 없는 주문입니다. orderId={}, status={}", orderId, record.status)
        }
    }

    private fun restoreAll(command: RestoreOrderStockCommand) {
        val requests = command.items.map { StockId.of(it.stockId) to it.quantity }

        val stocks = LinkedHashMap<StockId, Stock>()
        for (stockId in requests.map { it.first }.distinct().sortedBy { it.value }) {
            stocks[stockId] = stockRepository.findByIdForUpdate(stockId) ?: throw StockNotFoundException(stockId.toString())
        }
        for ((stockId, quantity) in requests) {
            stocks[stockId] = stocks.getValue(stockId).increase(quantity)
        }
        stocks.values.forEach { stockEventPublisher.publishStockChanged(it, stockRepository.save(it)) }
    }
}
