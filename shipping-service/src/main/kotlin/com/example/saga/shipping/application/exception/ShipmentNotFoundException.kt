package com.example.saga.shipping.application.exception

class ShipmentNotFoundException(val orderId: String) : ShipmentApplicationException("주문의 배송을 찾을 수 없습니다.")
