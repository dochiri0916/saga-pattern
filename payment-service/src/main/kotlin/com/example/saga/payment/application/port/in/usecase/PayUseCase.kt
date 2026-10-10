package com.example.saga.payment.application.port.`in`.usecase

import com.example.saga.payment.application.port.`in`.command.PayCommand
import com.example.saga.payment.application.port.`in`.result.PayResult

interface PayUseCase {

    fun pay(command: PayCommand): PayResult
}
