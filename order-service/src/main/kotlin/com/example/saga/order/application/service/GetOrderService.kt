package com.example.saga.order.application.service

import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.port.`in`.GetOrderUseCase
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.application.query.GetOrderQuery
import com.example.saga.order.application.result.GetOrderResult
import com.example.saga.order.domain.order.vo.OrderId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetOrderService(
    private val orderRepository: OrderRepository,
) : GetOrderUseCase {

    @Transactional(readOnly = true)
    override fun get(query: GetOrderQuery): GetOrderResult {
        val order = orderRepository.findById(OrderId.of(query.orderId))
            ?: throw OrderNotFoundException(query.orderId.toString())

        return GetOrderResult.from(order)
    }
}
