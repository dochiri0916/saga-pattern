package com.example.saga.inventory.application.port.`in`

import com.example.saga.inventory.application.query.GetStockQuery
import com.example.saga.inventory.application.result.GetStockResult

interface GetStockUseCase {

    fun get(query: GetStockQuery): GetStockResult
}
