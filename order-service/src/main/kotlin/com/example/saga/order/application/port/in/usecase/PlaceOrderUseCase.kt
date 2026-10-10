package com.example.saga.order.application.port.`in`.usecase

import com.example.saga.order.application.port.`in`.command.PlaceOrderCommand
import com.example.saga.order.application.port.`in`.result.PlaceOrderResult

interface PlaceOrderUseCase {

    fun place(command: PlaceOrderCommand): PlaceOrderResult
}
