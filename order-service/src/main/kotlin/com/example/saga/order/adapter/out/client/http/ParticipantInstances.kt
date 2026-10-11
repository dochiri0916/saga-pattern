package com.example.saga.order.adapter.out.client.http

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.web.client.RestClient

class ParticipantInstances(

    val name: String,
    private val instances: List<Instance>,
    private val ejectDuration: Duration,
    private val clock: Clock,
) {
    private val next = AtomicInteger()
    private val ejectedUntil = ConcurrentHashMap<String, Instant>()

    init {
        require(instances.isNotEmpty()) { "참여 서비스의 인스턴스가 하나도 없습니다." }
    }

    fun inOrder(): List<Instance> {
        val start = Math.floorMod(next.getAndIncrement(), instances.size)
        val now = clock.instant()
        return instances.indices
            .map { instances[(start + it) % instances.size] }
            .sortedBy { ejectedUntil[it.baseUrl]?.isAfter(now) == true }
    }

    fun eject(instance: Instance) {
        ejectedUntil[instance.baseUrl] = clock.instant().plus(ejectDuration)
    }

    data class Instance(val baseUrl: String, val restClient: RestClient)
}
