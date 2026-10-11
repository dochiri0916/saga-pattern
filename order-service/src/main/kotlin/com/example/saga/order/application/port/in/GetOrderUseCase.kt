package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.query.GetOrderQuery
import com.example.saga.order.application.result.GetOrderResult

interface GetOrderUseCase {

    fun get(query: GetOrderQuery): GetOrderResult
}
