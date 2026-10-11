package com.example.saga.payment.application.port.`in`

import com.example.saga.payment.application.command.CancelOrderPaymentCommand

interface CancelOrderPaymentUseCase {

    fun cancel(command: CancelOrderPaymentCommand)
}
