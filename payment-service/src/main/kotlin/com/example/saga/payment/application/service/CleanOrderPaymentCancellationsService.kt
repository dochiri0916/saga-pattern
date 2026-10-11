package com.example.saga.payment.application.service

import com.example.saga.payment.application.command.CleanOrderPaymentCancellationsCommand
import com.example.saga.payment.application.port.`in`.CleanOrderPaymentCancellationsUseCase
import com.example.saga.payment.application.port.out.OrderPaymentCancellationRepository
import java.time.Clock
import org.springframework.stereotype.Service

@Service
class CleanOrderPaymentCancellationsService(
    private val orderPaymentCancellationRepository: OrderPaymentCancellationRepository,
    private val clock: Clock,
) : CleanOrderPaymentCancellationsUseCase {

    override fun clean(command: CleanOrderPaymentCancellationsCommand): Int =
        orderPaymentCancellationRepository.deleteCancelledBefore(clock.instant().minus(command.retention), command.limit)
}
