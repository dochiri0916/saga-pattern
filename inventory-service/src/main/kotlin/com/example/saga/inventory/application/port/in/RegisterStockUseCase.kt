package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.command.RegisterStockCommand
import com.example.saga.inventory.application.result.RegisterStockResult

interface RegisterStockUseCase {

    fun register(command: RegisterStockCommand): RegisterStockResult

}
