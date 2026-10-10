package com.example.saga.order.domain.exception

import com.example.saga.order.domain.vo.StockReferenceId

class DuplicateOrderItemException(val stockReferenceId: StockReferenceId) : OrderDomainException()
