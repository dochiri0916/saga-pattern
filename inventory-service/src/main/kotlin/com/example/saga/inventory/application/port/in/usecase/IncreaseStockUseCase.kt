package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.command.IncreaseStockCommand
import com.example.saga.inventory.application.port.`in`.result.IncreaseStockResult

interface IncreaseStockUseCase {

    fun increase(command: IncreaseStockCommand): IncreaseStockResult
}
