package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.inventory.application.port.`in`.command.DecreaseStockCommand
import com.example.saga.inventory.application.port.`in`.result.DecreaseStockResult
import com.example.saga.inventory.application.port.`in`.usecase.DecreaseStockUseCase
import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.domain.vo.StockId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DecreaseStockService(
    private val stockRepository: StockRepository,
    private val stockEventPublisher: StockEventPublisher,
) : DecreaseStockUseCase {

    @Transactional
    override fun decrease(command: DecreaseStockCommand): DecreaseStockResult {
        val stock = stockRepository.findById(StockId.of(command.stockId))
            ?: throw StockNotFoundException(command.stockId.toString())

        val changed = stock.decrease(command.quantity)
        stockEventPublisher.publishStockChanged(changed, stockRepository.save(changed))

        return DecreaseStockResult.from(changed)
    }
}
