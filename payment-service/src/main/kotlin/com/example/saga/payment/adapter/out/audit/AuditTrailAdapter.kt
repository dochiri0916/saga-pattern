package com.example.saga.payment.adapter.out.audit

import com.example.saga.infrastructure.audit.AuditRecorder
import com.example.saga.payment.application.port.out.AuditTrail
import org.springframework.stereotype.Component

@Component
class AuditTrailAdapter(private val recorder: AuditRecorder) : AuditTrail {
    override fun record(aggregateId: String, action: String, detail: String?) =
        recorder.record("payment", aggregateId, action, detail)
}
