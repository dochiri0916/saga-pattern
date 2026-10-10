package com.example.saga.order.adapter.out.client

import com.example.saga.order.application.port.out.ParticipantCallException
import com.example.saga.order.application.port.out.ParticipantUnavailableException
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.http.HttpConnectTimeoutException
import java.nio.channels.UnresolvedAddressException

private val log = LoggerFactory.getLogger(ParticipantCaller::class.java)

private const val NOT_REJECTED = ""

@Component
class ParticipantCaller(
    private val jsonMapper: JsonMapper,
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    fun post(instances: ParticipantInstances, path: String, orderId: String, body: Any?, rejectionCode: String? = null): String? {
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker(instances.name)
        if (!circuitBreaker.tryAcquirePermission()) {
            throw ParticipantUnavailableException("회로가 열려 있어 요청을 보내지 않았습니다. participant=${instances.name}, path=$path, orderId=$orderId")
        }

        val startedAt = circuitBreaker.currentTimestamp
        try {
            val reason = send(instances, path, orderId, body, rejectionCode)
            circuitBreaker.onSuccess(circuitBreaker.currentTimestamp - startedAt, circuitBreaker.timestampUnit)
            return reason
        } catch (e: Exception) {
            circuitBreaker.onError(circuitBreaker.currentTimestamp - startedAt, circuitBreaker.timestampUnit, e)
            throw e
        }
    }

    private fun send(instances: ParticipantInstances, path: String, orderId: String, body: Any?, rejectionCode: String?): String? {
        var connectFailure: Exception? = null
        for (instance in instances.inOrder()) {
            try {
                return exchange(instance.restClient, path, orderId, body, rejectionCode)
            } catch (e: RestClientException) {
                if (!e.isConnectFailure()) {
                    throw ParticipantCallException("응답을 받지 못했습니다. instance=${instance.baseUrl}, path=$path, orderId=$orderId", e)
                }
                log.warn("연결하지 못해 다른 인스턴스로 넘어갑니다. instance={}, path={}, orderId={}", instance.baseUrl, path, orderId)
                instances.eject(instance)
                connectFailure = e
            } catch (e: IOException) {
                throw ParticipantCallException("응답을 읽지 못했습니다. instance=${instance.baseUrl}, path=$path, orderId=$orderId", e)
            }
        }
        throw ParticipantUnavailableException("연결할 수 있는 인스턴스가 없습니다. path=$path, orderId=$orderId", connectFailure)
    }

    private fun exchange(restClient: RestClient, path: String, orderId: String, body: Any?, rejectionCode: String?): String? {
        val request = restClient.post().uri(path, orderId)
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).body(body)

        val reason = request.exchange { _, response ->
            val status = response.statusCode
            when {
                status.is2xxSuccessful -> NOT_REJECTED
                status.value() == HttpStatus.CONFLICT.value() && rejectionCode != null ->
                    rejectionReason(response.body.readAllBytes(), rejectionCode)
                        ?: throw ParticipantCallException("예상하지 못한 409 응답입니다. path=$path, orderId=$orderId")
                else -> throw ParticipantCallException("오류 응답을 받았습니다. status=${status.value()}, path=$path, orderId=$orderId")
            }
        }
        return reason?.takeIf { it != NOT_REJECTED }
    }

    private fun rejectionReason(body: ByteArray, rejectionCode: String): String? {
        val problem = try {
            jsonMapper.readValue(body, Map::class.java)
        } catch (e: JacksonException) {
            return null
        }
        if (problem["code"] != rejectionCode) return null
        return problem["reason"] as? String ?: rejectionCode
    }

    private fun Throwable.isConnectFailure(): Boolean = generateSequence(this) { it.cause }.any {
        it is ConnectException || it is HttpConnectTimeoutException || it is UnknownHostException || it is UnresolvedAddressException
    }
}
