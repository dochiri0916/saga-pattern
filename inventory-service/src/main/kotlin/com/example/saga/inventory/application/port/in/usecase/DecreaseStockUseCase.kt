package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.command.DecreaseStockCommand
import com.example.saga.inventory.application.port.`in`.result.DecreaseStockResult

interface DecreaseStockUseCase {

    fun decrease(command: DecreaseStockCommand): DecreaseStockResult
}
