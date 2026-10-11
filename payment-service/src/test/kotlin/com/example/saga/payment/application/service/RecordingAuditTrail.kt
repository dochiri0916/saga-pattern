package com.example.saga.payment.application.service

import com.example.saga.payment.application.port.out.AuditTrail

internal class RecordingAuditTrail : AuditTrail {
    val actions = mutableListOf<String>()
    override fun record(aggregateId: String, action: String, detail: String?) { actions += action }
}
