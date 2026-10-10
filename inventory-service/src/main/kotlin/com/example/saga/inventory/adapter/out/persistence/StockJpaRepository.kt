package com.example.saga.inventory.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.UUID

interface StockJpaRepository : JpaRepository<StockEntity, Long> {

    fun findByPublicId(publicId: UUID): StockEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByPublicId(publicId: UUID): StockEntity?

}
