package com.example.saga.inventory.application.exception

class StockNotFoundException(val stockId: String) : StockApplicationException("재고를 찾을 수 없습니다.")
