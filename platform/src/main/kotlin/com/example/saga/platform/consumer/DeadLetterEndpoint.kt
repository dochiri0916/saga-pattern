package com.example.saga.platform.consumer

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation

@Endpoint(id = "deadletters")
class DeadLetterEndpoint(
    private val deadLetterRedriver: DeadLetterRedriver,
) {

    @ReadOperation
    fun pending(): Map<String, Int> = mapOf("pending" to deadLetterRedriver.pending())

    @WriteOperation
    fun redrive(): Map<String, Int> = mapOf("redriven" to deadLetterRedriver.redrive())
}
