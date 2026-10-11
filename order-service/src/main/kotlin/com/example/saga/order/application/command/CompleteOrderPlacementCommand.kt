package com.example.saga.order.application.command

import java.util.UUID

data class CompleteOrderPlacementCommand(val orderId: UUID)
