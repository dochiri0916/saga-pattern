package com.example.saga.order

import java.security.MessageDigest
import java.sql.DriverManager
import java.time.Instant
import java.util.HexFormat
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ScriptUtils

class SchemaMigrationIntegrationTest {
    @Test
    fun `기존 멱등 기록을 새 인프라 테이블로 보존하고 스크립트 재실행도 허용한다`() {
        DriverManager.getConnection("jdbc:tc:mysql:8.4:///schema_migration_it", "test", "test").use { connection ->
            val orderId = UUID.randomUUID().toString()
            val key = "existing-key"
            val requestHash = "a".repeat(64)
            val createdAt = Instant.parse("2026-10-10T00:00:00Z")
            connection.createStatement().use { statement ->
                statement.execute("create table order_idempotency_keys (idempotency_key varchar(64) primary key, order_id binary(16) not null, request_hash varchar(64) not null, created_at datetime(6) not null)")
            }
            connection.prepareStatement("insert into order_idempotency_keys values (?, UUID_TO_BIN(?), ?, ?)").use { statement ->
                statement.setString(1, key)
                statement.setString(2, orderId)
                statement.setString(3, requestHash)
                statement.setTimestamp(4, java.sql.Timestamp.from(createdAt))
                statement.executeUpdate()
            }
            repeat(2) {
                listOf("01_audit_trail.sql", "02_order_infrastructure.sql", "03_migrate_order_idempotency.sql").forEach { name ->
                    ScriptUtils.executeSqlScript(connection, ClassPathResource("db/refactoring/$name"))
                }
            }
            connection.createStatement().use { statement ->
                statement.executeQuery("select * from http_idempotency").use { result ->
                    assertTrue(result.next())
                    val scope = "POST /orders"
                    val expectedId = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest("${scope.length}:$scope$key".toByteArray()))
                    assertEquals(expectedId, result.getString("id"))
                    assertEquals(scope, result.getString("scope"))
                    assertEquals(key, result.getString("idempotency_key"))
                    assertEquals(requestHash, result.getString("request_hash"))
                    assertEquals(orderId, result.getString("resource_id"))
                    assertEquals(createdAt, result.getTimestamp("created_at").toInstant())
                    assertFalse(result.next())
                }
                statement.executeQuery("select count(*) from order_idempotency_keys").use { result ->
                    assertTrue(result.next())
                    assertEquals(1, result.getInt(1))
                }
            }
        }
    }
}
