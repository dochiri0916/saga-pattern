package com.example.saga.platform.web

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import java.time.Clock
import java.time.Instant

class ProblemDetailFactory(private val clock: Clock) {

    fun create(status: HttpStatus, code: String, detail: String?): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)

        problemDetail.title = status.reasonPhrase
        problemDetail.setProperty("code", code)
        problemDetail.setProperty("timestamp", Instant.now(clock))

        return problemDetail
    }

    fun complete(problemDetail: ProblemDetail, status: HttpStatusCode): ProblemDetail {
        if (problemDetail.properties?.containsKey("code") == true) return problemDetail

        problemDetail.setProperty("code", status.codeOf())
        problemDetail.setProperty("timestamp", Instant.now(clock))

        return problemDetail
    }

    private fun HttpStatusCode.codeOf(): String = HttpStatus.resolve(value())?.name ?: "HTTP_${value()}"
}
