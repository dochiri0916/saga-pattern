package com.example.saga.order.adapter.`in`.web

import com.example.saga.infrastructure.web.ProblemDetailFactory
import com.example.saga.order.application.exception.OrderCancelPeriodExpiredException
import com.example.saga.order.application.exception.OrderCancelRejectedException
import com.example.saga.order.application.exception.OrderConcurrentUpdateException
import com.example.saga.order.application.exception.OrderNotFoundException
import com.example.saga.order.application.exception.OrderRejectedException
import com.example.saga.order.application.exception.OrderSagaNotFoundException
import com.example.saga.order.application.exception.StockNotFoundException
import com.example.saga.order.application.service.MutableClock
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OrderApplicationExceptionHandlerTest {

    private val handler = OrderApplicationExceptionHandler(ProblemDetailFactory(MutableClock(Instant.parse("2026-10-10T00:00:00Z"))))

    @Test
    @DisplayName("취소 기간이 지난 주문의 예외를 처리하면 취소 불가 메시지와 기간 만료 사유를 포함한 409 응답을 반환한다")
    fun returnsCancellationConflictWhenCancelPeriodHasExpired() {
        // given
        val exception = OrderCancelPeriodExpiredException("order-1")

        // when
        val problem = handler.handle(exception)

        // then
        assertEquals(409, problem.status)
        assertEquals("ORDER_CANCEL_REJECTED", problem.properties?.get("code"))
        assertEquals("주문을 취소할 수 없습니다.", problem.detail)
        assertEquals("order-1", problem.properties?.get("orderId"))
        assertEquals("CANCEL_PERIOD_EXPIRED", problem.properties?.get("reason"))
    }

    @Test
    @DisplayName("참여자가 주문 취소를 거절한 예외를 처리하면 취소 불가 메시지와 거절 사유를 포함한 409 응답을 반환한다")
    fun returnsCancellationConflictWhenParticipantRejectsCancellation() {
        // given
        val exception = OrderCancelRejectedException("order-1", "ALREADY_SHIPPED")

        // when
        val problem = handler.handle(exception)

        // then
        assertEquals(409, problem.status)
        assertEquals("ORDER_CANCEL_REJECTED", problem.properties?.get("code"))
        assertEquals("주문을 취소할 수 없습니다.", problem.detail)
        assertEquals("order-1", problem.properties?.get("orderId"))
        assertEquals("ALREADY_SHIPPED", problem.properties?.get("reason"))
    }

    @Test
    @DisplayName("주문을 찾지 못한 예외를 처리하면 주문 조회 실패 메시지와 ORDER_NOT_FOUND 코드를 포함한 404 응답을 반환한다")
    fun returnsOrderNotFoundResponseWhenOrderDoesNotExist() {
        // given
        val exception = OrderNotFoundException("order-1")

        // when
        val problem = handler.handle(exception)

        // then
        assertEquals(404, problem.status)
        assertEquals("ORDER_NOT_FOUND", problem.properties?.get("code"))
        assertEquals("주문을 찾을 수 없습니다.", problem.detail)
    }

    @Test
    @DisplayName("주문의 사가를 찾지 못한 예외를 처리하면 사가 조회 실패 메시지와 ORDER_SAGA_NOT_FOUND 코드를 포함한 404 응답을 반환한다")
    fun returnsSagaNotFoundResponseWhenOrderSagaDoesNotExist() {
        // given
        val exception = OrderSagaNotFoundException("order-1")

        // when
        val problem = handler.handle(exception)

        // then
        assertEquals(404, problem.status)
        assertEquals("ORDER_SAGA_NOT_FOUND", problem.properties?.get("code"))
        assertEquals("주문의 사가를 찾을 수 없습니다.", problem.detail)
    }

    @Test
    @DisplayName("주문할 재고를 찾지 못한 예외를 처리하면 재고 조회 실패 메시지와 STOCK_NOT_FOUND 코드를 포함한 404 응답을 반환한다")
    fun returnsStockNotFoundResponseWhenStockDoesNotExist() {
        // given
        val exception = StockNotFoundException("stock-1")

        // when
        val problem = handler.handle(exception)

        // then
        assertEquals(404, problem.status)
        assertEquals("STOCK_NOT_FOUND", problem.properties?.get("code"))
        assertEquals("주문할 수 없는 재고입니다.", problem.detail)
    }

    @Test
    @DisplayName("주문 처리가 거절된 예외를 처리하면 주문 취소 메시지와 거절 사유를 포함한 409 응답을 반환한다")
    fun returnsOrderConflictWhenOrderPlacementIsRejected() {
        // given
        val exception = OrderRejectedException("order-1", "OUT_OF_STOCK")

        // when
        val problem = handler.handle(exception)

        // then
        assertEquals(409, problem.status)
        assertEquals("ORDER_REJECTED", problem.properties?.get("code"))
        assertEquals("주문을 처리할 수 없어 취소했습니다.", problem.detail)
        assertEquals("order-1", problem.properties?.get("orderId"))
        assertEquals("OUT_OF_STOCK", problem.properties?.get("reason"))
    }

    @Test
    @DisplayName("원인이 있는 주문 동시 변경 예외를 처리하면 원인을 보존하고 내부 오류 대신 재시도 안내를 포함한 409 응답을 반환한다")
    fun preservesCauseAndReturnsRetryMessageWhenOrderIsConcurrentlyUpdated() {
        // given
        val cause = IllegalStateException("내부 데이터베이스 오류")
        val exception = OrderConcurrentUpdateException("order-1", cause)

        // when
        val problem = handler.handle(exception)

        // then
        assertSame(cause, exception.cause)
        assertEquals(409, problem.status)
        assertEquals("ORDER_CONCURRENT_UPDATE", problem.properties?.get("code"))
        assertEquals("다른 요청이 동시에 주문을 변경했습니다. 다시 시도해주세요.", problem.detail)
    }
}
