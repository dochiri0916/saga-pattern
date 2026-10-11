package com.example.saga.order.adapter.out.persistence

import com.example.saga.order.adapter.out.persistence.mapper.toDomain
import com.example.saga.order.adapter.out.persistence.mapper.toEntity
import com.example.saga.order.adapter.out.persistence.mapper.update
import com.example.saga.order.adapter.out.persistence.repository.OrderJpaRepository
import com.example.saga.order.application.exception.OrderConcurrentUpdateException
import com.example.saga.order.application.port.out.OrderRepository
import com.example.saga.order.domain.order.Order
import com.example.saga.order.domain.order.vo.OrderId
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = orderJpaRepository.findByPublicId(order.id.value)
            ?.apply { update(order) }
            ?: order.toEntity()

        try {
            orderJpaRepository.saveAndFlush(entity)
        } catch (e: OptimisticLockingFailureException) {
            throw OrderConcurrentUpdateException(order.id.toString(), e)
        }
        return order
    }

    override fun findById(id: OrderId): Order? =
        orderJpaRepository.findByPublicId(id.value)?.toDomain()

}
