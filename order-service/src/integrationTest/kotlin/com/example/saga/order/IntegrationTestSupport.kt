package com.example.saga.order

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun <T> race(vararg tasks: () -> T): List<Result<T>> {
    val ready = CountDownLatch(tasks.size)
    val go = CountDownLatch(1)
    val pool = Executors.newFixedThreadPool(tasks.size)
    try {
        val futures = tasks.map { task ->
            pool.submit(
                Callable {
                    ready.countDown()
                    go.await()
                    runCatching(task)
                }
            )
        }
        ready.await()
        go.countDown()
        return futures.map { it.get(60, TimeUnit.SECONDS) }
    } finally {
        pool.shutdownNow()
    }
}

const val MYSQL_URL = "spring.datasource.url=jdbc:tc:mysql:8.4:///it"
const val CREATE_TABLES = "spring.jpa.hibernate.ddl-auto=create"
const val POOL_SIZE = "spring.datasource.hikari.maximum-pool-size=30"
const val NO_OUTBOX = "saga.outbox.enabled=false"
const val NO_CONSUMER = "saga.consumer.enabled=false"
