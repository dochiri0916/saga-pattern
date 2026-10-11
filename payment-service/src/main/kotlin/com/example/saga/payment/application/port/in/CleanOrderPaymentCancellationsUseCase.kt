package com.example.saga.payment.application.port.`in`

import com.example.saga.payment.application.command.CleanOrderPaymentCancellationsCommand

interface CleanOrderPaymentCancellationsUseCase {

    fun clean(command: CleanOrderPaymentCancellationsCommand): Int
}
