package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.command.RestoreOrderStockCommand

interface RestoreOrderStockUseCase {

    fun restore(command: RestoreOrderStockCommand)
}
