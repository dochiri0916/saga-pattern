package com.example.saga.order.adapter.`in`.messaging

import com.example.saga.order.adapter.`in`.requireV7
import com.example.saga.order.application.port.`in`.command.UpdateStockSnapshotCommand
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class StockChangedMessage(
    val stockId: UUID,
    val name: String,
    val price: Int,
    val quantity: Int,
    val version: Long,
) {
    fun toCommand(): UpdateStockSnapshotCommand = UpdateStockSnapshotCommand(stockId.requireV7("stockId"), name, price, quantity, version)

    companion object {
        const val EVENT_TYPE = "STOCK_CHANGED"
    }
}
