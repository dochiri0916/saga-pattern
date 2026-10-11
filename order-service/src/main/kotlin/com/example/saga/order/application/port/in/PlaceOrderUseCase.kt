package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.command.PlaceOrderCommand
import com.example.saga.order.application.result.PlaceOrderResult

interface PlaceOrderUseCase {

    fun place(command: PlaceOrderCommand): PlaceOrderResult
}
