package com.example.saga.inventory.adapter.`in`.web.controller

import com.example.saga.inventory.adapter.`in`.requireV7
import com.example.saga.inventory.adapter.`in`.web.mapper.toCommand
import com.example.saga.inventory.adapter.`in`.web.mapper.toResponse
import com.example.saga.inventory.adapter.`in`.web.request.DecreaseStockRequest
import com.example.saga.inventory.adapter.`in`.web.request.IncreaseStockRequest
import com.example.saga.inventory.adapter.`in`.web.request.RegisterStockRequest
import com.example.saga.inventory.adapter.`in`.web.response.DecreaseStockResponse
import com.example.saga.inventory.adapter.`in`.web.response.GetStockResponse
import com.example.saga.inventory.adapter.`in`.web.response.IncreaseStockResponse
import com.example.saga.inventory.adapter.`in`.web.response.RegisterStockResponse
import com.example.saga.inventory.application.port.`in`.DecreaseStockUseCase
import com.example.saga.inventory.application.port.`in`.GetStockUseCase
import com.example.saga.inventory.application.port.`in`.IncreaseStockUseCase
import com.example.saga.inventory.application.port.`in`.RegisterStockUseCase
import com.example.saga.inventory.application.query.GetStockQuery
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/stocks")
class StockController(
    private val registerStockUseCase: RegisterStockUseCase,
    private val getStockUseCase: GetStockUseCase,
    private val decreaseStockUseCase: DecreaseStockUseCase,
    private val increaseStockUseCase: IncreaseStockUseCase,
) {

    @PostMapping
    fun register(@RequestBody request: RegisterStockRequest): ResponseEntity<RegisterStockResponse> {
        val response = registerStockUseCase.register(request.toCommand()).toResponse()
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{stockId}")
            .buildAndExpand(response.stockId)
            .toUri()

        return ResponseEntity.created(location).body(response)
    }

    @GetMapping("/{stockId}")
    fun get(@PathVariable stockId: UUID): GetStockResponse =
        getStockUseCase.get(GetStockQuery(stockId.requireV7("stockId"))).toResponse()

    @PostMapping("/{stockId}/decrease")
    fun decrease(
        @PathVariable stockId: UUID,
        @RequestBody request: DecreaseStockRequest,
    ): DecreaseStockResponse =
        decreaseStockUseCase.decrease(request.toCommand(stockId.requireV7("stockId"))).toResponse()

    @PostMapping("/{stockId}/increase")
    fun increase(
        @PathVariable stockId: UUID,
        @RequestBody request: IncreaseStockRequest,
    ): IncreaseStockResponse =
        increaseStockUseCase.increase(request.toCommand(stockId.requireV7("stockId"))).toResponse()
}
