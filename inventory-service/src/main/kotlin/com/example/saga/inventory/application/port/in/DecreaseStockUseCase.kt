package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.command.DecreaseStockCommand
import com.example.saga.inventory.application.result.DecreaseStockResult

interface DecreaseStockUseCase {

    fun decrease(command: DecreaseStockCommand): DecreaseStockResult
}
