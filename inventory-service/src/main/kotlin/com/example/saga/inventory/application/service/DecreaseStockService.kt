package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.command.DecreaseStockCommand
import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.inventory.application.integration.mapper.publishStockChanged
import com.example.saga.inventory.application.port.`in`.DecreaseStockUseCase
import com.example.saga.inventory.application.port.out.AuditTrail
import com.example.saga.inventory.application.port.out.EventPublicationPort
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.result.DecreaseStockResult
import com.example.saga.inventory.domain.stock.vo.StockId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DecreaseStockService(
    private val stockRepository: StockRepository,
    private val stockEventPublisher: EventPublicationPort,
    private val auditTrail: AuditTrail,
) : DecreaseStockUseCase {

    @Transactional
    override fun decrease(command: DecreaseStockCommand): DecreaseStockResult {
        val stock = stockRepository.findById(StockId.of(command.stockId))
            ?: throw StockNotFoundException(command.stockId.toString())

        val changed = stock.decrease(command.quantity)
        stockEventPublisher.publishStockChanged(changed, stockRepository.save(changed))
        auditTrail.record(changed.id.toString(), "STOCK_DECREASED")

        return DecreaseStockResult.from(changed)
    }
}
