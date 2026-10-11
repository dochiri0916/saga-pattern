package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.command.UpdateStockSnapshotCommand

interface UpdateStockSnapshotUseCase {

    fun update(command: UpdateStockSnapshotCommand)
}
