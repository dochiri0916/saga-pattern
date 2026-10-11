package com.example.saga.infrastructure.idempotency

class IdempotencyInterceptor(private val manager: IdempotencyManager) {
    fun <T> execute(
        key: String?, scope: String, canonicalRequest: String,
        prepare: () -> String, onCreated: (String) -> T, onReplay: (String) -> T,
    ): T {
        if (key == null) return onCreated(prepare())
        if (key.isBlank() || key.length > 64) throw InvalidIdempotencyKeyException()
        val reservation = manager.reserve(scope, key, canonicalRequest, prepare)
        return if (reservation.created) onCreated(reservation.resourceId) else onReplay(reservation.resourceId)
    }
}
