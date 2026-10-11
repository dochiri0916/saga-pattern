package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.command.CancelOrderCommand
import com.example.saga.order.application.result.CancelOrderResult

interface CancelOrderUseCase {

    fun cancel(command: CancelOrderCommand): CancelOrderResult
}
