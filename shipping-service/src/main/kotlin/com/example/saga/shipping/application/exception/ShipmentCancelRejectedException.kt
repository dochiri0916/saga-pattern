package com.example.saga.shipping.application.exception

class ShipmentCancelRejectedException(
    val orderId: String,
    val reason: ShipmentCancelRejectionReason,
) : ShipmentApplicationException("주문의 배송을 취소할 수 없습니다.")

enum class ShipmentCancelRejectionReason {
    ALREADY_SHIPPED,
}
