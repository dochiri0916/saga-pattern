package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.command.RegisterStockCommand
import com.example.saga.inventory.application.port.`in`.result.RegisterStockResult

interface RegisterStockUseCase {

    fun register(command: RegisterStockCommand): RegisterStockResult

}
