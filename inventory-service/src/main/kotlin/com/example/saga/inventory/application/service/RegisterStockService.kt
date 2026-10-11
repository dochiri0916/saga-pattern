package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.command.RegisterStockCommand
import com.example.saga.inventory.application.integration.mapper.publishStockChanged
import com.example.saga.inventory.application.port.`in`.RegisterStockUseCase
import com.example.saga.inventory.application.port.out.AuditTrail
import com.example.saga.inventory.application.port.out.EventPublicationPort
import com.example.saga.inventory.application.port.out.StockIdGenerator
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.result.RegisterStockResult
import com.example.saga.inventory.domain.stock.Stock
import com.example.saga.inventory.domain.stock.vo.StockName
import com.example.saga.inventory.domain.stock.vo.StockPrice
import com.example.saga.inventory.domain.stock.vo.StockQuantity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterStockService(
    private val stockIdGenerator: StockIdGenerator,
    private val stockRepository: StockRepository,
    private val stockEventPublisher: EventPublicationPort,
    private val auditTrail: AuditTrail,
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
        auditTrail.record(stock.id.toString(), "STOCK_REGISTERED")

        return RegisterStockResult.from(stock)
    }
}
