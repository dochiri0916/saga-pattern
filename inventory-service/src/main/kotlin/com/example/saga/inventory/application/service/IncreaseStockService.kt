package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.inventory.application.port.`in`.command.IncreaseStockCommand
import com.example.saga.inventory.application.port.`in`.result.IncreaseStockResult
import com.example.saga.inventory.application.port.`in`.usecase.IncreaseStockUseCase
import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.domain.vo.StockId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IncreaseStockService(
    private val stockRepository: StockRepository,
    private val stockEventPublisher: StockEventPublisher,
) : IncreaseStockUseCase {

    @Transactional
    override fun increase(command: IncreaseStockCommand): IncreaseStockResult {
        val stock = stockRepository.findById(StockId.of(command.stockId))
            ?: throw StockNotFoundException(command.stockId.toString())

        val changed = stock.increase(command.quantity)
        stockEventPublisher.publishStockChanged(changed, stockRepository.save(changed))

        return IncreaseStockResult.from(changed)
    }
}
