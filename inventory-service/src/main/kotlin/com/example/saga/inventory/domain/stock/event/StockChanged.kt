package com.example.saga.inventory.domain.stock.event

import com.example.saga.inventory.domain.stock.Stock
import com.example.saga.inventory.domain.stock.vo.StockRevision

data class StockChanged(val stock: Stock, val revision: StockRevision)
