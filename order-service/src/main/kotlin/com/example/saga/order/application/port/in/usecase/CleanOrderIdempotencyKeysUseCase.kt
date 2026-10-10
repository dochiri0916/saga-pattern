package com.example.saga.order.application.port.`in`.usecase

import com.example.saga.order.application.port.`in`.command.CleanOrderIdempotencyKeysCommand

interface CleanOrderIdempotencyKeysUseCase {

    fun clean(command: CleanOrderIdempotencyKeysCommand): Int
}
