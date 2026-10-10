package com.example.saga.order.application.port.`in`.usecase

import com.example.saga.order.application.port.`in`.command.CancelOrderCommand
import com.example.saga.order.application.port.`in`.result.CancelOrderResult

interface CancelOrderUseCase {

    fun cancel(command: CancelOrderCommand): CancelOrderResult
}
