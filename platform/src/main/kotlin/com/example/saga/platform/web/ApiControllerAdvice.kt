package com.example.saga.platform.web

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private val log = LoggerFactory.getLogger(ApiControllerAdvice::class.java)

private const val INTERNAL_ERROR_CODE = "INTERNAL_ERROR"
private const val INTERNAL_ERROR_DETAIL = "일시적인 오류가 발생했습니다."
private const val INVALID_INPUT_CODE = "INVALID_INPUT"
private const val INVALID_INPUT_DETAIL = "요청 입력 형식을 확인해주세요."

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class ApiControllerAdvice(private val problemDetailFactory: ProblemDetailFactory) : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.warn("요청 입력 형식이 올바르지 않습니다.")
        val problem = problemDetailFactory.create(HttpStatus.BAD_REQUEST, INVALID_INPUT_CODE, INVALID_INPUT_DETAIL)
        return handleExceptionInternal(exception, problem, headers, status, request)
    }

    override fun handleHttpMessageNotReadable(
        exception: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.warn("요청 본문을 읽을 수 없습니다.")
        val problem = problemDetailFactory.create(HttpStatus.BAD_REQUEST, INVALID_INPUT_CODE, INVALID_INPUT_DETAIL)
        return handleExceptionInternal(exception, problem, headers, status, request)
    }

    override fun handleExceptionInternal(
        exception: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val completed = if (body is ProblemDetail) problemDetailFactory.complete(body, statusCode) else body
        return super.handleExceptionInternal(exception, completed, headers, statusCode, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(exception: Exception, request: HttpServletRequest): ProblemDetail {
        log.error("미처리 예외가 발생했습니다. uri={}, method={}", request.requestURI, request.method, exception)
        return problemDetailFactory.create(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_CODE, INTERNAL_ERROR_DETAIL)
    }
}
