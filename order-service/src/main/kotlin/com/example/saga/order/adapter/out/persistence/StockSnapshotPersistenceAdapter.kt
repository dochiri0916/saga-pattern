package com.example.saga.order.adapter.out.persistence

import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.domain.vo.StockReferenceId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class StockSnapshotPersistenceAdapter(
    private val stockSnapshotJpaRepository: StockSnapshotJpaRepository,
) : StockSnapshotRepository {

    override fun saveIfNewer(stockSnapshot: StockSnapshot): Boolean = stockSnapshotJpaRepository.upsertIfNewer(
        stockId = stockSnapshot.stockReferenceId.value,
        name = stockSnapshot.name,
        price = stockSnapshot.price,
        quantity = stockSnapshot.quantity,
        version = stockSnapshot.version,
    ) > 0

    override fun findById(stockReferenceId: StockReferenceId): StockSnapshot? =
        stockSnapshotJpaRepository.findByIdOrNull(stockReferenceId.value)?.toReadModel()

    private fun StockSnapshotEntity.toReadModel(): StockSnapshot = StockSnapshot(
        stockReferenceId = StockReferenceId.of(stockId),
        name = name,
        price = price,
        quantity = quantity,
        version = version,
    )
}
