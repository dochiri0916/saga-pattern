package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.command.RestoreOrderStockCommand

interface RestoreOrderStockUseCase {

    fun restore(command: RestoreOrderStockCommand)
}
