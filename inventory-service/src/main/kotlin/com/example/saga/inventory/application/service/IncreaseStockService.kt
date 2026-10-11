package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.command.IncreaseStockCommand
import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.inventory.application.integration.mapper.publishStockChanged
import com.example.saga.inventory.application.port.`in`.IncreaseStockUseCase
import com.example.saga.inventory.application.port.out.AuditTrail
import com.example.saga.inventory.application.port.out.EventPublicationPort
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.result.IncreaseStockResult
import com.example.saga.inventory.domain.stock.vo.StockId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IncreaseStockService(
    private val stockRepository: StockRepository,
    private val stockEventPublisher: EventPublicationPort,
    private val auditTrail: AuditTrail,
) : IncreaseStockUseCase {

    @Transactional
    override fun increase(command: IncreaseStockCommand): IncreaseStockResult {
        val stock = stockRepository.findById(StockId.of(command.stockId))
            ?: throw StockNotFoundException(command.stockId.toString())

        val changed = stock.increase(command.quantity)
        stockEventPublisher.publishStockChanged(changed, stockRepository.save(changed))
        auditTrail.record(changed.id.toString(), "STOCK_INCREASED")

        return IncreaseStockResult.from(changed)
    }
}
