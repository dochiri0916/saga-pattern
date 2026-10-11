package com.example.saga.inventory.application.exception

import com.example.saga.inventory.domain.orderstock.vo.StockDecreaseRejectionReason

class OrderStockDecreaseRejectedException(
    val orderId: String,
    val reason: StockDecreaseRejectionReason,

    val stockId: String?,
) : StockApplicationException("주문의 재고를 차감할 수 없습니다.")
