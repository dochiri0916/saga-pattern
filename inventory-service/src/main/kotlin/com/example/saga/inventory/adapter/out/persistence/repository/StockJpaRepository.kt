package com.example.saga.inventory.adapter.out.persistence.repository

import com.example.saga.inventory.adapter.out.persistence.entity.StockEntity
import jakarta.persistence.LockModeType
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface StockJpaRepository : JpaRepository<StockEntity, Long> {

    fun findByPublicId(publicId: UUID): StockEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findForUpdateByPublicId(publicId: UUID): StockEntity?

}
