package com.example.saga.payment.application.exception

class PaymentRejectedException(
    val orderId: String,
    val reason: PaymentRejectionReason,
) : PaymentApplicationException("주문을 결제할 수 없습니다.")

enum class PaymentRejectionReason {
    INVALID_AMOUNT,
    GATEWAY_DECLINED,

    PAYMENT_CANCELLED,

    ORDER_CANCELLED,
}
