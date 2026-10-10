package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.command.CleanOrderStockRecordsCommand

interface CleanOrderStockRecordsUseCase {

    fun clean(command: CleanOrderStockRecordsCommand): Int
}
