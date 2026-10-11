package com.example.saga.payment.application.port.`in`

import com.example.saga.payment.application.command.PayCommand
import com.example.saga.payment.application.result.PayResult

interface PayUseCase {

    fun pay(command: PayCommand): PayResult
}
