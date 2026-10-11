package com.example.saga.order.application.port.`in`

import com.example.saga.order.application.command.RecoverStalledOrderSagasCommand

interface RecoverStalledOrderSagasUseCase {

    fun recover(command: RecoverStalledOrderSagasCommand): Int
}
