package com.example.saga.order.application.service

import com.example.saga.order.application.port.`in`.command.CleanOrderIdempotencyKeysCommand
import com.example.saga.order.application.port.`in`.usecase.CleanOrderIdempotencyKeysUseCase
import com.example.saga.order.application.port.out.OrderIdempotencyKeyRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class CleanOrderIdempotencyKeysService(
    private val orderIdempotencyKeyRepository: OrderIdempotencyKeyRepository,
    private val clock: Clock,
) : CleanOrderIdempotencyKeysUseCase {

    override fun clean(command: CleanOrderIdempotencyKeysCommand): Int =
        orderIdempotencyKeyRepository.deleteCreatedBefore(clock.instant().minus(command.retention), command.limit)
}
