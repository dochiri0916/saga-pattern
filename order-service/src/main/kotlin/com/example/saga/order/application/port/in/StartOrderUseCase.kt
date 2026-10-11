package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.command.PlaceOrderCommand
import com.example.saga.order.application.result.StartedOrderResult

interface StartOrderUseCase { fun start(command: PlaceOrderCommand): StartedOrderResult }
