package com.example.saga.inventory.adapter.`in`.web.controller

import com.example.saga.inventory.adapter.`in`.requireV7
import com.example.saga.inventory.adapter.`in`.web.mapper.toCommand
import com.example.saga.inventory.adapter.`in`.web.request.DecreaseOrderStockRequest
import com.example.saga.inventory.adapter.`in`.web.request.RestoreOrderStockRequest
import com.example.saga.inventory.application.port.`in`.DecreaseOrderStockUseCase
import com.example.saga.inventory.application.port.`in`.RestoreOrderStockUseCase
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order-stocks/{orderId}")
class OrderStockController(
    private val decreaseOrderStockUseCase: DecreaseOrderStockUseCase,
    private val restoreOrderStockUseCase: RestoreOrderStockUseCase,
) {

    @PostMapping("/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun decrease(@PathVariable orderId: UUID, @RequestBody request: DecreaseOrderStockRequest) =
        decreaseOrderStockUseCase.decrease(request.toCommand(orderId.requireV7("orderId")))

    @PostMapping("/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun restore(@PathVariable orderId: UUID, @RequestBody request: RestoreOrderStockRequest) =
        restoreOrderStockUseCase.restore(request.toCommand(orderId.requireV7("orderId")))
}
