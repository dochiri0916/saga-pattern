package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.command.CleanOrderStockRecordsCommand

interface CleanOrderStockRecordsUseCase {

    fun clean(command: CleanOrderStockRecordsCommand): Int
}
