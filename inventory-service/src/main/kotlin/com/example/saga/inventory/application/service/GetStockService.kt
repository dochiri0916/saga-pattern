package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.exception.StockNotFoundException
import com.example.saga.inventory.application.port.`in`.GetStockUseCase
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.query.GetStockQuery
import com.example.saga.inventory.application.result.GetStockResult
import com.example.saga.inventory.domain.stock.vo.StockId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetStockService(
    private val stockRepository: StockRepository,
) : GetStockUseCase {

    @Transactional(readOnly = true)
    override fun get(query: GetStockQuery): GetStockResult {
        val stock = stockRepository.findById(StockId.of(query.stockId))
            ?: throw StockNotFoundException(query.stockId.toString())

        return GetStockResult.from(stock)
    }
}
