package com.example.saga.inventory.application.service

import com.example.saga.inventory.application.port.out.AuditTrail

internal class RecordingAuditTrail : AuditTrail {
    val actions = mutableListOf<String>()
    override fun record(aggregateId: String, action: String, detail: String?) { actions += action }
}
