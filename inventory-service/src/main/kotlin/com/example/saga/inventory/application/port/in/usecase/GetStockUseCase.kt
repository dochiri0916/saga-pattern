package com.example.saga.inventory.application.port.`in`.usecase

import com.example.saga.inventory.application.port.`in`.query.GetStockQuery
import com.example.saga.inventory.application.port.`in`.result.GetStockResult

interface GetStockUseCase {

    fun get(query: GetStockQuery): GetStockResult
}
