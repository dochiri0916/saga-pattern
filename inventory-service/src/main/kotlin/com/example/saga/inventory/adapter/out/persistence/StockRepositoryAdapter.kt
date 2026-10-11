package com.example.saga.inventory.adapter.out.persistence

import com.example.saga.inventory.adapter.out.persistence.mapper.toDomain
import com.example.saga.inventory.adapter.out.persistence.mapper.toEntity
import com.example.saga.inventory.adapter.out.persistence.repository.StockJpaRepository
import com.example.saga.inventory.application.exception.StockConcurrentUpdateException
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.domain.stock.Stock
import com.example.saga.inventory.domain.stock.vo.StockId
import com.example.saga.inventory.domain.stock.vo.StockRevision
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
class StockRepositoryAdapter(
    private val stockJpaRepository: StockJpaRepository,
) : StockRepository {

    override fun save(stock: Stock): StockRevision {
        val entity = stockJpaRepository.findByPublicId(stock.id.value)
            ?.apply { update(stock.name.value, stock.quantity.value, stock.price.value) }
            ?: stock.toEntity()

        val saved = try {
            stockJpaRepository.saveAndFlush(entity)
        } catch (e: OptimisticLockingFailureException) {
            throw StockConcurrentUpdateException(stock.id.toString(), e)
        }

        return StockRevision(checkNotNull(saved.version) { "저장된 재고에는 version이 있어야 합니다." })
    }

    override fun findById(id: StockId): Stock? =
        stockJpaRepository.findByPublicId(id.value)?.toDomain()

    override fun findByIdForUpdate(id: StockId): Stock? =
        stockJpaRepository.findForUpdateByPublicId(id.value)?.toDomain()

}
