package com.example.saga.shipping.contract.command

data class CancelOrderShipment(val orderId: String)
data class ShipOrderShipment(val orderId: String)
