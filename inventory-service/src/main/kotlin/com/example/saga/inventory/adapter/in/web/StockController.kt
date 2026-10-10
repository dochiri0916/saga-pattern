package com.example.saga.inventory.adapter.`in`.web

import com.example.saga.inventory.adapter.`in`.web.request.DecreaseStockRequest
import com.example.saga.inventory.adapter.`in`.web.request.IncreaseStockRequest
import com.example.saga.inventory.adapter.`in`.web.request.RegisterStockRequest
import com.example.saga.inventory.adapter.`in`.web.response.DecreaseStockResponse
import com.example.saga.inventory.adapter.`in`.web.response.GetStockResponse
import com.example.saga.inventory.adapter.`in`.web.response.IncreaseStockResponse
import com.example.saga.inventory.adapter.`in`.web.response.RegisterStockResponse
import com.example.saga.inventory.adapter.`in`.requireV7
import com.example.saga.inventory.application.port.`in`.query.GetStockQuery
import com.example.saga.inventory.application.port.`in`.usecase.DecreaseStockUseCase
import com.example.saga.inventory.application.port.`in`.usecase.GetStockUseCase
import com.example.saga.inventory.application.port.`in`.usecase.IncreaseStockUseCase
import com.example.saga.inventory.application.port.`in`.usecase.RegisterStockUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

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
        val response = RegisterStockResponse.from(registerStockUseCase.register(request.toCommand()))
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{stockId}")
            .buildAndExpand(response.stockId)
            .toUri()

        return ResponseEntity.created(location).body(response)
    }

    @GetMapping("/{stockId}")
    fun get(@PathVariable stockId: UUID): GetStockResponse =
        GetStockResponse.from(getStockUseCase.get(GetStockQuery(stockId.requireV7("stockId"))))

    @PostMapping("/{stockId}/decrease")
    fun decrease(
        @PathVariable stockId: UUID,
        @RequestBody request: DecreaseStockRequest,
    ): DecreaseStockResponse =
        DecreaseStockResponse.from(decreaseStockUseCase.decrease(request.toCommand(stockId.requireV7("stockId"))))

    @PostMapping("/{stockId}/increase")
    fun increase(
        @PathVariable stockId: UUID,
        @RequestBody request: IncreaseStockRequest,
    ): IncreaseStockResponse =
        IncreaseStockResponse.from(increaseStockUseCase.increase(request.toCommand(stockId.requireV7("stockId"))))
}
