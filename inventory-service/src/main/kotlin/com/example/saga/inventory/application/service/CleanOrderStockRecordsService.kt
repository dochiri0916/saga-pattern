package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.command.CleanOrderStockRecordsCommand
import com.example.saga.inventory.application.port.`in`.CleanOrderStockRecordsUseCase
import com.example.saga.inventory.application.port.out.OrderStockRecordRepository
import com.example.saga.inventory.domain.orderstock.vo.OrderStockStatus
import java.time.Clock
import org.springframework.stereotype.Service

@Service
class CleanOrderStockRecordsService(
    private val orderStockRecordRepository: OrderStockRecordRepository,
    private val clock: Clock,
) : CleanOrderStockRecordsUseCase {

    override fun clean(command: CleanOrderStockRecordsCommand): Int {
        val now = clock.instant()
        val finished = orderStockRecordRepository.deleteUpdatedBefore(FINISHED, now.minus(command.retention), command.limit)
        if (finished == command.limit) return finished
        return finished + orderStockRecordRepository.deleteUpdatedBefore(
            statuses = listOf(OrderStockStatus.DECREASED),
            before = now.minus(command.decreasedRetention),
            limit = command.limit - finished,
        )
    }

    private companion object {
        val FINISHED = listOf(OrderStockStatus.REJECTED, OrderStockStatus.RESTORED, OrderStockStatus.CANCELLED)
    }
}
