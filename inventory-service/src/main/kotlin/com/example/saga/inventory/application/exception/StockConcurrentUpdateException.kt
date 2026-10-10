package com.example.saga.inventory.application.exception

class StockConcurrentUpdateException(val stockId: String, cause: Throwable? = null) :
    StockApplicationException("다른 요청이 동시에 재고를 변경했습니다. 다시 시도해주세요.", cause)
