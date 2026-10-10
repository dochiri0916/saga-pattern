package com.example.saga.order.application.port.`in`.usecase

import com.example.saga.order.application.port.`in`.query.GetOrderQuery
import com.example.saga.order.application.port.`in`.result.GetOrderResult

interface GetOrderUseCase {

    fun get(query: GetOrderQuery): GetOrderResult
}
