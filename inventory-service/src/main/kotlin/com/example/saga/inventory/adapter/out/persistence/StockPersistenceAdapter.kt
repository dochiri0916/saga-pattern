package com.example.saga.inventory.adapter.out.persistence

import com.example.saga.inventory.application.exception.StockConcurrentUpdateException
import com.example.saga.inventory.application.port.out.StockRepository
import com.example.saga.inventory.application.port.out.StockRevision
import com.example.saga.inventory.domain.Stock
import com.example.saga.inventory.domain.vo.StockId
import com.example.saga.inventory.domain.vo.StockName
import com.example.saga.inventory.domain.vo.StockPrice
import com.example.saga.inventory.domain.vo.StockQuantity
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
class StockPersistenceAdapter(
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

    private fun Stock.toEntity(): StockEntity = StockEntity.of(
        publicId = id.value,
        name = name.value,
        quantity = quantity.value,
        price = price.value,
    )

    private fun StockEntity.toDomain(): Stock = Stock.restore(
        id = StockId.of(publicId),
        name = StockName.of(name),
        quantity = StockQuantity.of(quantity),
        price = StockPrice.of(price),
    )
}
