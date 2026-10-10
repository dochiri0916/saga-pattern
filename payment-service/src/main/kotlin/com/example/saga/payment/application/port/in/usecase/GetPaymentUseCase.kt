package com.example.saga.payment.application.port.`in`.usecase

import com.example.saga.payment.application.port.`in`.query.GetPaymentQuery
import com.example.saga.payment.application.port.`in`.result.GetPaymentResult

interface GetPaymentUseCase {

    fun get(query: GetPaymentQuery): GetPaymentResult
}
