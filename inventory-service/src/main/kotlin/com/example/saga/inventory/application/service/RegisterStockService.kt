package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.port.`in`.command.RegisterStockCommand
import com.example.saga.inventory.application.port.`in`.result.RegisterStockResult
import com.example.saga.inventory.application.port.`in`.usecase.RegisterStockUseCase
import com.example.saga.inventory.application.port.out.StockEventPublisher
import com.example.saga.inventory.application.port.out.StockIdGenerator
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.domain.Stock
import com.example.saga.inventory.domain.vo.StockName
import com.example.saga.inventory.domain.vo.StockPrice
import com.example.saga.inventory.domain.vo.StockQuantity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterStockService(
    private val stockIdGenerator: StockIdGenerator,
    private val stockRepository: StockRepository,
    private val stockEventPublisher: StockEventPublisher,
) : RegisterStockUseCase {

    @Transactional
    override fun register(command: RegisterStockCommand): RegisterStockResult {
        val stock = Stock.register(
            id = stockIdGenerator.generate(),
            name = StockName.of(command.name),
            quantity = StockQuantity.of(command.quantity),
            price = StockPrice.of(command.price),
        )

        stockEventPublisher.publishStockChanged(stock, stockRepository.save(stock))

        return RegisterStockResult.from(stock)
    }
}
