package com.example.saga.payment.application.service

import com.example.saga.payment.application.port.`in`.command.CleanOrderPaymentCancellationsCommand
import com.example.saga.payment.application.port.`in`.usecase.CleanOrderPaymentCancellationsUseCase
import com.example.saga.payment.application.port.out.OrderPaymentCancellationRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class CleanOrderPaymentCancellationsService(
    private val orderPaymentCancellationRepository: OrderPaymentCancellationRepository,
    private val clock: Clock,
) : CleanOrderPaymentCancellationsUseCase {

    override fun clean(command: CleanOrderPaymentCancellationsCommand): Int =
        orderPaymentCancellationRepository.deleteCancelledBefore(clock.instant().minus(command.retention), command.limit)
}
