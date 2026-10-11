package com.example.saga.order.application.service

import com.example.saga.order.application.command.UpdateStockSnapshotCommand
import com.example.saga.order.application.port.`in`.UpdateStockSnapshotUseCase
import com.example.saga.order.application.port.out.StockSnapshotRepository
import com.example.saga.order.application.readmodel.StockSnapshot
import com.example.saga.order.domain.order.vo.StockReferenceId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(UpdateStockSnapshotService::class.java)

@Service
class UpdateStockSnapshotService(
    private val stockSnapshotRepository: StockSnapshotRepository,
) : UpdateStockSnapshotUseCase {

    @Transactional
    override fun update(command: UpdateStockSnapshotCommand) {
        val snapshot = StockSnapshot(
            stockReferenceId = StockReferenceId.of(command.stockId),
            name = command.name,
            price = command.price,
            quantity = command.quantity,
            version = command.version,
        )
        if (!stockSnapshotRepository.saveIfNewer(snapshot)) {
            log.debug("가진 스냅샷보다 낡은 상태라서 버립니다. stockId={}, version={}", snapshot.stockReferenceId, snapshot.version)
        }
    }
}
