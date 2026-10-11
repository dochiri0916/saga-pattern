package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.query.GetOrderPlacementQuery
import com.example.saga.order.application.result.PlaceOrderResult

interface GetOrderPlacementUseCase { fun placement(query: GetOrderPlacementQuery): PlaceOrderResult }
