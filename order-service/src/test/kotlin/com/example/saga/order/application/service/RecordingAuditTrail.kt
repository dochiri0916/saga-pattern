package com.example.saga.order.application.service

import com.example.saga.order.application.port.out.AuditTrail

internal class RecordingAuditTrail : AuditTrail {
    val actions = mutableListOf<String>()
    override fun record(aggregateId: String, action: String, detail: String?) { actions += action }
}
