package com.example.saga.inventory.adapter.out.audit

import com.example.saga.infrastructure.audit.AuditRecorder
import com.example.saga.inventory.application.port.out.AuditTrail
import org.springframework.stereotype.Component

@Component
class AuditTrailAdapter(private val recorder: AuditRecorder) : AuditTrail {
    override fun record(aggregateId: String, action: String, detail: String?) =
        recorder.record("inventory", aggregateId, action, detail)
}
