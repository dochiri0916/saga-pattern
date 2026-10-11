package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.command.DecreaseOrderStockCommand

interface DecreaseOrderStockUseCase {

    fun decrease(command: DecreaseOrderStockCommand)
}
