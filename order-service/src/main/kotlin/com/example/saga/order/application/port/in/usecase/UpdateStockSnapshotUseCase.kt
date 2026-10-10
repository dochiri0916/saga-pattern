package com.example.saga.order.application.port.`in`.usecase

import com.example.saga.order.application.port.`in`.command.UpdateStockSnapshotCommand

interface UpdateStockSnapshotUseCase {

    fun update(command: UpdateStockSnapshotCommand)
}
