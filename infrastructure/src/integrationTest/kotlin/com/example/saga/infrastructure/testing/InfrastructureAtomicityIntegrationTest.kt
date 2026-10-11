package com.example.saga.infrastructure.testing

import com.example.saga.infrastructure.audit.AuditRecorder
import com.example.saga.infrastructure.idempotency.IdempotencyKeyReusedException
import com.example.saga.infrastructure.idempotency.IdempotencyManager
import com.example.saga.infrastructure.messaging.inbox.InboxGuard
import com.example.saga.infrastructure.messaging.inbox.InboxJpaRepository
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootConfiguration
@EnableAutoConfiguration
class InfrastructureIntegrationTestConfig

@SpringBootTest(classes = [InfrastructureIntegrationTestConfig::class], properties = [
    "spring.datasource.url=jdbc:tc:mysql:8.4:///infrastructure_it",
    "spring.jpa.hibernate.ddl-auto=create", "spring.datasource.hikari.maximum-pool-size=20",
    "saga.outbox.enabled=false", "saga.consumer.enabled=false", "saga.idempotency.enabled=true",
    "saga.inbox.enabled=true", "saga.inbox.cleanup.interval=1h",
    "saga.inbox.cleanup.retention=8d", "saga.inbox.cleanup.batch-size=1000",
])
class InfrastructureAtomicityIntegrationTest @Autowired constructor(
    private val inboxGuard: InboxGuard,
    private val inboxRepository: InboxJpaRepository,
    private val idempotencyManager: IdempotencyManager,
    private val auditRecorder: AuditRecorder,
    private val jdbc: JdbcTemplate,
) {
    private fun auditCount(id: String): Int = jdbc.queryForObject(
        "select count(*) from audit_trail where aggregate_id = ?", Int::class.java, id,
    )!!

    @Test
    fun `핸들러 실패는 Inbox와 감사 기록을 함께 롤백하고 재전달은 다시 처리한다`() {
        val eventId = UUID.randomUUID()
        val id = eventId.toString()
        assertFailsWith<IllegalStateException> {
            inboxGuard.execute(eventId) {
                auditRecorder.record("test", id, "HANDLED", null)
                error("핸들러 실패")
            }
        }
        assertFalse(inboxRepository.existsById(eventId))
        assertEquals(0, auditCount(id))
        assertTrue(inboxGuard.execute(eventId) { auditRecorder.record("test", id, "HANDLED", null) })
        assertFalse(inboxGuard.execute(eventId) { error("중복 핸들러를 호출하면 안 됩니다.") })
        assertTrue(inboxRepository.existsById(eventId))
        assertEquals(1, auditCount(id))
    }

    @Test
    fun `같은 이벤트를 동시에 전달해도 핸들러는 한 번만 실행한다`() {
        val eventId = UUID.randomUUID()
        val handled = AtomicInteger()
        val results = race(8) {
            inboxGuard.execute(eventId) {
                handled.incrementAndGet()
                auditRecorder.record("test", eventId.toString(), "HANDLED", null)
            }
        }
        assertEquals(1, results.count { it })
        assertEquals(1, handled.get())
        assertEquals(1, auditCount(eventId.toString()))
    }

    @Test
    fun `HTTP 준비 실패는 멱등 키와 업무 기록을 롤백한다`() {
        val key = UUID.randomUUID().toString()
        assertFailsWith<IllegalStateException> {
            idempotencyManager.reserve("POST /test", key, "body") {
                auditRecorder.record("test", key, "CREATED", null)
                error("준비 실패")
            }
        }
        assertEquals(0, jdbc.queryForObject("select count(*) from http_idempotency where idempotency_key = ?", Int::class.java, key))
        assertEquals(0, auditCount(key))
        val first = idempotencyManager.reserve("POST /test", key, "body") {
            auditRecorder.record("test", key, "CREATED", null)
            UUID.randomUUID().toString()
        }
        val repeated = idempotencyManager.reserve("POST /test", key, "body") { error("중복 준비") }
        assertTrue(first.created)
        assertFalse(repeated.created)
        assertEquals(first.resourceId, repeated.resourceId)
        assertEquals(1, auditCount(key))
        assertFailsWith<IdempotencyKeyReusedException> {
            idempotencyManager.reserve("POST /test", key, "other body") { error("다른 내용의 준비") }
        }
        val anotherScope = idempotencyManager.reserve("POST /other", key, "body") { UUID.randomUUID().toString() }
        assertTrue(anotherScope.created)
        assertNotEquals(first.resourceId, anotherScope.resourceId)
    }

    @Test
    fun `같은 HTTP 키의 동시 요청은 하나의 리소스를 준비한다`() {
        val key = UUID.randomUUID().toString()
        val prepared = AtomicInteger()
        val results = race(8) {
            idempotencyManager.reserve("POST /test", key, "body") {
                prepared.incrementAndGet()
                auditRecorder.record("test", key, "CREATED", null)
                UUID.randomUUID().toString()
            }
        }
        assertEquals(1, results.count { it.created })
        assertEquals(1, results.map { it.resourceId }.toSet().size)
        assertEquals(1, prepared.get())
        assertEquals(1, auditCount(key))
    }

    private fun <T> race(count: Int, task: () -> T): List<T> {
        val ready = CountDownLatch(count)
        val go = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(count)
        try {
            val futures = List(count) { pool.submit(Callable { ready.countDown(); go.await(); task() }) }
            assertTrue(ready.await(10, TimeUnit.SECONDS))
            go.countDown()
            return futures.map { it.get(60, TimeUnit.SECONDS) }
        } finally {
            pool.shutdownNow()
        }
    }
}
