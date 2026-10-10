package com.example.saga.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {

    fun findByPublicId(publicId: UUID): OrderEntity?
}
