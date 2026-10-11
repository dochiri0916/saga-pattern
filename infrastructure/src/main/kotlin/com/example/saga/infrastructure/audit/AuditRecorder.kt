package com.example.saga.infrastructure.audit

import java.time.Clock
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(propagation = Propagation.MANDATORY)
class AuditRecorder(private val repository: AuditRepository, private val clock: Clock) {
    fun record(context: String, aggregateId: String, action: String, detail: String?) {
        repository.save(AuditJpaEntity.of(context, aggregateId, action, detail, clock.instant()))
    }
}
