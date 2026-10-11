package com.example.saga.infrastructure.audit

import org.springframework.data.jpa.repository.JpaRepository

interface AuditRepository : JpaRepository<AuditJpaEntity, Long>
