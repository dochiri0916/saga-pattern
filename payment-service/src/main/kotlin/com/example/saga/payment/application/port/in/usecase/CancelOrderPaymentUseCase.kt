package com.example.saga.payment.application.port.`in`.usecase

import com.example.saga.payment.application.port.`in`.command.CancelOrderPaymentCommand

interface CancelOrderPaymentUseCase {

    fun cancel(command: CancelOrderPaymentCommand)
}
