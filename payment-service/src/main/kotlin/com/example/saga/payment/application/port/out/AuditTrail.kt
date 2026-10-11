package com.example.saga.payment.application.port.out

interface AuditTrail { fun record(aggregateId: String, action: String, detail: String? = null) }
