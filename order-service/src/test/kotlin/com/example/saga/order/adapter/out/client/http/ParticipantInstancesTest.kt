package com.example.saga.order.adapter.out.client.http

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.web.client.RestClient

class ParticipantInstancesTest {

    private var now = Instant.parse("2026-10-10T00:00:00Z")
    private val clock = object : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
    }
    private val a = ParticipantInstances.Instance("http://a", RestClient.create())
    private val b = ParticipantInstances.Instance("http://b", RestClient.create())
    private val c = ParticipantInstances.Instance("http://c", RestClient.create())
    private val instances = ParticipantInstances("inventory", listOf(a, b, c), Duration.ofSeconds(5), clock)

    private fun order(): List<String> = instances.inOrder().map { it.baseUrl.removePrefix("http://") }

    @Test
    fun `호출할 때마다 시작 위치를 돌린다`() {
        assertEquals(listOf("a", "b", "c"), order())
        assertEquals(listOf("b", "c", "a"), order())
        assertEquals(listOf("c", "a", "b"), order())
        assertEquals(listOf("a", "b", "c"), order())
    }

    @Test
    fun `연결하지 못한 인스턴스는 맨 뒤로 미루되 시도 대상에서 빼지는 않는다`() {
        instances.eject(a)

        assertEquals(listOf("b", "c", "a"), order())
        assertEquals(listOf("b", "c", "a"), order())
        assertEquals(listOf("c", "b", "a"), order())
    }

    @Test
    fun `미룬 시간이 지나면 다시 원래 순서로 돌아온다`() {
        instances.eject(a)
        now = now.plusSeconds(6)

        assertEquals(listOf("a", "b", "c"), order())
    }

    @Test
    fun `전부 미뤄졌어도 모두 시도한다`() {
        listOf(a, b, c).forEach { instances.eject(it) }

        assertEquals(setOf("a", "b", "c"), order().toSet())
    }
}
