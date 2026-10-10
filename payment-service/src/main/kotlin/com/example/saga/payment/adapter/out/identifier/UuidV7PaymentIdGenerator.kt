package com.example.saga.payment.adapter.out.identifier

import com.example.saga.payment.application.port.out.PaymentIdGenerator
import com.example.saga.payment.domain.vo.PaymentId
import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.stereotype.Component

@Component
class UuidV7PaymentIdGenerator : PaymentIdGenerator {
    override fun generate(): PaymentId = PaymentId.of(UuidCreator.getTimeOrderedEpoch())
}
