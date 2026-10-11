package com.example.saga.shipping.application.service

import com.example.saga.shipping.application.port.out.AuditTrail

internal class RecordingAuditTrail : AuditTrail {
    val actions = mutableListOf<String>()
    override fun record(aggregateId: String, action: String, detail: String?) { actions += action }
}
