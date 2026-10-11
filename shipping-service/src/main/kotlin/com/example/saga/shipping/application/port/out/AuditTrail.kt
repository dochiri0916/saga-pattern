package com.example.saga.shipping.application.port.out

interface AuditTrail { fun record(aggregateId: String, action: String, detail: String? = null) }
