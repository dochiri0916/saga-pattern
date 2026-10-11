package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.command.IncreaseStockCommand
import com.example.saga.inventory.application.result.IncreaseStockResult

interface IncreaseStockUseCase {

    fun increase(command: IncreaseStockCommand): IncreaseStockResult
}
