package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.command.DecreaseOrderStockCommand

interface DecreaseOrderStockUseCase {

    fun decrease(command: DecreaseOrderStockCommand)
}
