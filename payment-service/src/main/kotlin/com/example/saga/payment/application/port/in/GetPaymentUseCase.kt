package com.example.saga.payment.application.port.`in`

import com.example.saga.payment.application.query.GetPaymentQuery
import com.example.saga.payment.application.result.GetPaymentResult

interface GetPaymentUseCase {

    fun get(query: GetPaymentQuery): GetPaymentResult
}
