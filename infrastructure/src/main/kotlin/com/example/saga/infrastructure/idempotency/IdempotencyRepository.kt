package com.example.saga.infrastructure.idempotency

import jakarta.persistence.LockModeType
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface IdempotencyRepository : JpaRepository<IdempotencyJpaEntity, String> {
    @Modifying
    @Query(value = "insert into http_idempotency (id, scope, idempotency_key, request_hash, created_at) values (:id, :scope, :key, :requestHash, :createdAt) on duplicate key update id = http_idempotency.id", nativeQuery = true)
    fun insertOrLock(id: String, scope: String, key: String, requestHash: String, createdAt: Instant): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select entry from IdempotencyJpaEntity entry where entry.id = :id")
    fun findForUpdate(id: String): IdempotencyJpaEntity?

    @Modifying
    @Query("update IdempotencyJpaEntity entry set entry.resourceId = :resourceId where entry.id = :id")
    fun attach(id: String, resourceId: String): Int

    @Transactional
    @Modifying
    @Query(value = "delete from http_idempotency where created_at < :cutoff limit :batchSize", nativeQuery = true)
    fun deleteCreatedBefore(cutoff: Instant, batchSize: Int): Int
}
