package com.example.saga.order.application.port.`in`.usecase

import com.example.saga.order.application.port.`in`.command.RecoverStalledOrderSagasCommand

interface RecoverStalledOrderSagasUseCase {

    fun recover(command: RecoverStalledOrderSagasCommand): Int
}
