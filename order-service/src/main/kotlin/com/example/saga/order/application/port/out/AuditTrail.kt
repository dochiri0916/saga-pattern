package com.example.saga.order.application.port.out

interface AuditTrail { fun record(aggregateId: String, action: String, detail: String? = null) }
