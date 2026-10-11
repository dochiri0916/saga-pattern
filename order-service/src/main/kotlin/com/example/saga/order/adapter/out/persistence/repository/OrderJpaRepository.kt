package com.example.saga.order.adapter.out.persistence.repository

import com.example.saga.order.adapter.out.persistence.entity.OrderEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {

    fun findByPublicId(publicId: UUID): OrderEntity?
}
