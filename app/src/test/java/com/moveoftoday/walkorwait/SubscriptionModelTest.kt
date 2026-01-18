package com.moveoftoday.walkorwait

import org.junit.Assert.*
import org.junit.Test

/**
 * SubscriptionModel 단위 테스트
 *
 * 구독 크레딧 계산 로직을 검증합니다.
 * - 95% 이상: 4,900 크레딧 (실질 무료)
 * - 80-94%: 2,400 크레딧 (실질 2,500원)
 * - 80% 미만: 0 크레딧 (정가 4,900원)
 */
class SubscriptionModelTest {

    // ===== 크레딧 계산 테스트 =====

    @Test
    fun `95% 달성시 4900 크레딧 지급`() {
        val credit = SubscriptionModel.getCreditAmount(95f)
        assertEquals(4900, credit)
    }

    @Test
    fun `100% 달성시 4900 크레딧 지급`() {
        val credit = SubscriptionModel.getCreditAmount(100f)
        assertEquals(4900, credit)
    }

    @Test
    fun `99% 달성시 4900 크레딧 지급`() {
        val credit = SubscriptionModel.getCreditAmount(99f)
        assertEquals(4900, credit)
    }

    @Test
    fun `94% 달성시 2400 크레딧 지급`() {
        val credit = SubscriptionModel.getCreditAmount(94f)
        assertEquals(2400, credit)
    }

    @Test
    fun `80% 달성시 2400 크레딧 지급`() {
        val credit = SubscriptionModel.getCreditAmount(80f)
        assertEquals(2400, credit)
    }

    @Test
    fun `85% 달성시 2400 크레딧 지급`() {
        val credit = SubscriptionModel.getCreditAmount(85f)
        assertEquals(2400, credit)
    }

    @Test
    fun `79% 달성시 크레딧 없음`() {
        val credit = SubscriptionModel.getCreditAmount(79f)
        assertEquals(0, credit)
    }

    @Test
    fun `50% 달성시 크레딧 없음`() {
        val credit = SubscriptionModel.getCreditAmount(50f)
        assertEquals(0, credit)
    }

    @Test
    fun `0% 달성시 크레딧 없음`() {
        val credit = SubscriptionModel.getCreditAmount(0f)
        assertEquals(0, credit)
    }

    // ===== 실질 부담 금액 테스트 =====

    @Test
    fun `95% 이상 달성시 실질 무료`() {
        assertEquals(0, SubscriptionModel.getEffectivePrice(95f))
        assertEquals(0, SubscriptionModel.getEffectivePrice(100f))
    }

    @Test
    fun `80-94% 달성시 실질 2500원`() {
        assertEquals(2500, SubscriptionModel.getEffectivePrice(80f))
        assertEquals(2500, SubscriptionModel.getEffectivePrice(94f))
        assertEquals(2500, SubscriptionModel.getEffectivePrice(87f))
    }

    @Test
    fun `80% 미만 달성시 정가 4900원`() {
        assertEquals(4900, SubscriptionModel.getEffectivePrice(79f))
        assertEquals(4900, SubscriptionModel.getEffectivePrice(50f))
        assertEquals(4900, SubscriptionModel.getEffectivePrice(0f))
    }

    // ===== 할인 금액 테스트 =====

    @Test
    fun `95% 이상시 4900원 할인`() {
        assertEquals(4900, SubscriptionModel.getDiscountAmount(95f))
    }

    @Test
    fun `80-94%시 2400원 할인`() {
        assertEquals(2400, SubscriptionModel.getDiscountAmount(85f))
    }

    @Test
    fun `80% 미만시 할인 없음`() {
        assertEquals(0, SubscriptionModel.getDiscountAmount(79f))
    }

    // ===== 티어 분류 테스트 =====

    @Test
    fun `95% 이상은 FREE 티어`() {
        assertEquals(SubscriptionModel.Tier.FREE, SubscriptionModel.getTier(95f))
        assertEquals(SubscriptionModel.Tier.FREE, SubscriptionModel.getTier(100f))
    }

    @Test
    fun `80-94%는 DISCOUNT 티어`() {
        assertEquals(SubscriptionModel.Tier.DISCOUNT, SubscriptionModel.getTier(80f))
        assertEquals(SubscriptionModel.Tier.DISCOUNT, SubscriptionModel.getTier(94f))
    }

    @Test
    fun `80% 미만은 PENALTY 티어`() {
        assertEquals(SubscriptionModel.Tier.PENALTY, SubscriptionModel.getTier(79f))
        assertEquals(SubscriptionModel.Tier.PENALTY, SubscriptionModel.getTier(0f))
    }

    // ===== 상태 텍스트 테스트 =====

    @Test
    fun `95% 이상시 다음 달 무료 텍스트`() {
        assertEquals("다음 달 무료!", SubscriptionModel.getStatusText(95f))
    }

    @Test
    fun `80-94%시 다음 달 2500원 텍스트`() {
        assertEquals("다음 달 2,500원", SubscriptionModel.getStatusText(85f))
    }

    @Test
    fun `80% 미만시 다음 달 4900원 텍스트`() {
        assertEquals("다음 달 4,900원", SubscriptionModel.getStatusText(50f))
    }

    // ===== 가격 포맷팅 테스트 =====

    @Test
    fun `0원은 무료로 표시`() {
        assertEquals("무료", SubscriptionModel.formatPrice(0))
    }

    @Test
    fun `4900원 포맷팅`() {
        assertEquals("4,900원", SubscriptionModel.formatPrice(4900))
    }

    @Test
    fun `2500원 포맷팅`() {
        assertEquals("2,500원", SubscriptionModel.formatPrice(2500))
    }

    // ===== 경계값 테스트 =====

    @Test
    fun `경계값 94점99는 DISCOUNT 티어`() {
        val credit = SubscriptionModel.getCreditAmount(94.99f)
        assertEquals(2400, credit)
    }

    @Test
    fun `경계값 79점99는 PENALTY 티어`() {
        val credit = SubscriptionModel.getCreditAmount(79.99f)
        assertEquals(0, credit)
    }

    // ===== 상수값 검증 =====

    @Test
    fun `기본 가격은 4700원`() {
        assertEquals(4700, SubscriptionModel.MONTHLY_PRICE)
    }

    @Test
    fun `무료 기준은 95%`() {
        assertEquals(95, SubscriptionModel.THRESHOLD_FREE)
    }

    @Test
    fun `할인 기준은 80%`() {
        assertEquals(80, SubscriptionModel.THRESHOLD_DISCOUNT)
    }
}
