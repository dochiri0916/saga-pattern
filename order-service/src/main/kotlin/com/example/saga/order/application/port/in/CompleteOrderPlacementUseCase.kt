package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.command.CompleteOrderPlacementCommand
import com.example.saga.order.application.result.PlaceOrderResult

interface CompleteOrderPlacementUseCase { fun complete(command: CompleteOrderPlacementCommand): PlaceOrderResult }
