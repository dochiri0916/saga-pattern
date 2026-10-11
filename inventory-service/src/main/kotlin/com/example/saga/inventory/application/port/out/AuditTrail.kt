package com.example.saga.inventory.application.port.out

interface AuditTrail { fun record(aggregateId: String, action: String, detail: String? = null) }
