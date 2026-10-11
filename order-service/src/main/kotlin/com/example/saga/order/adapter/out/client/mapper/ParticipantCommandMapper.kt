package com.example.saga.order.adapter.out.client.mapper

import com.example.saga.inventory.contract.command.DecreaseOrderStock
import com.example.saga.inventory.contract.command.OrderStockItem
import com.example.saga.inventory.contract.command.RestoreOrderStock
import com.example.saga.order.domain.order.Order
import com.example.saga.payment.contract.command.CancelOrderPayment
import com.example.saga.payment.contract.command.PayOrder
import com.example.saga.shipping.contract.command.CancelOrderShipment

object ParticipantCommandMapper {
    fun decrease(order: Order) = DecreaseOrderStock(items(order))
    fun restore(order: Order) = RestoreOrderStock(items(order))
    fun pay(order: Order) = PayOrder(order.id.toString(), order.totalAmount.value)
    fun cancelPayment(order: Order) = CancelOrderPayment(order.id.toString())
    fun cancelShipment(order: Order) = CancelOrderShipment(order.id.toString())
    private fun items(order: Order) = order.items.map { OrderStockItem(it.stockReferenceId.toString(), it.quantity.value) }
}
