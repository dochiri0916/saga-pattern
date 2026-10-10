package com.example.saga.order.application.exception

class StockNotFoundException(val stockId: String) : OrderApplicationException("주문할 수 없는 재고입니다.")
