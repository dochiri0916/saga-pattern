package com.example.saga.infrastructure.idempotency

import java.security.MessageDigest
import java.time.Clock
import java.util.HexFormat
import org.springframework.transaction.annotation.Transactional

data class IdempotencyReservation(val resourceId: String, val created: Boolean)
class IdempotencyKeyReusedException(val key: String) : RuntimeException("같은 멱등 키를 다른 요청 내용으로 쓸 수 없습니다.")
class InvalidIdempotencyKeyException : IllegalArgumentException("Idempotency-Key는 비어 있지 않은 64자 이하의 문자열이어야 합니다.")

@Transactional
class IdempotencyManager(private val repository: IdempotencyRepository, private val clock: Clock) {
    fun reserve(scope: String, key: String, canonicalRequest: String, create: () -> String): IdempotencyReservation {
        val id = hash("${scope.length}:$scope$key")
        val requestHash = hash(canonicalRequest)
        repository.insertOrLock(id, scope, key, requestHash, clock.instant())
        val existing = checkNotNull(repository.findForUpdate(id)) { "저장된 멱등 키를 찾지 못했습니다." }
        if (existing.requestHash != requestHash) throw IdempotencyKeyReusedException(key)
        if (existing.resourceId == null) {
            val resourceId = create()
            check(repository.attach(id, resourceId) == 1)
            return IdempotencyReservation(resourceId, true)
        }
        return IdempotencyReservation(checkNotNull(existing.resourceId), false)
    }

    private fun hash(value: String): String = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)))
}
