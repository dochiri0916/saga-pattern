package com.example.saga.payment.application.port.`in`.usecase

import com.example.saga.payment.application.port.`in`.command.CleanOrderPaymentCancellationsCommand

interface CleanOrderPaymentCancellationsUseCase {

    fun clean(command: CleanOrderPaymentCancellationsCommand): Int
}
