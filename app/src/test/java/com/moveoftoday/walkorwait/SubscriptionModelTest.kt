package com.moveoftoday.walkorwait

import org.junit.Assert.*
import org.junit.Test

/**
 * SubscriptionModel 단위 테스트
 *
 * 구독 모델 검증:
 * - 월간 3,900원, 연간 39,000원
 * - 친구 초대: 연간 12명, 월간 1명
 */
class SubscriptionModelTest {

    // ===== 가격 상수 테스트 =====

    @Test
    fun `월간 가격은 3900원`() {
        assertEquals(3900, SubscriptionModel.MONTHLY_PRICE)
    }

    @Test
    fun `연간 가격은 39000원`() {
        assertEquals(39000, SubscriptionModel.YEARLY_PRICE)
    }

    // ===== 친구 초대 수 테스트 =====

    @Test
    fun `연간 구독자는 12명 초대 가능`() {
        assertEquals(12, SubscriptionModel.getInviteCount(isYearly = true))
    }

    @Test
    fun `월간 구독자는 1명 초대 가능`() {
        assertEquals(1, SubscriptionModel.getInviteCount(isYearly = false))
    }

    @Test
    fun `초대 기간은 30일`() {
        assertEquals(30, SubscriptionModel.INVITE_DURATION_DAYS)
    }

    // ===== 연간 구독 절약 금액 테스트 =====

    @Test
    fun `연간 구독시 7800원 절약`() {
        // 월간 12개월 = 3900 * 12 = 46,800원
        // 연간 = 39,000원
        // 절약 = 7,800원
        assertEquals(7800, SubscriptionModel.getYearlySavings())
    }

    // ===== 가격 포맷팅 테스트 =====

    @Test
    fun `0원은 무료로 표시`() {
        assertEquals("무료", SubscriptionModel.formatPrice(0))
    }

    @Test
    fun `3900원 포맷팅`() {
        assertEquals("3,900원", SubscriptionModel.formatPrice(3900))
    }

    @Test
    fun `39000원 포맷팅`() {
        assertEquals("39,000원", SubscriptionModel.formatPrice(39000))
    }

    // ===== FriendInviteSystem 테스트 =====

    @Test
    fun `Guest 무료 사용 기간은 30일`() {
        assertEquals(30, FriendInviteSystem.GUEST_DURATION_DAYS)
    }

    @Test
    fun `연간 구독자 최대 Guest는 12명`() {
        assertEquals(12, FriendInviteSystem.YEARLY_MAX_GUESTS)
    }

    @Test
    fun `월간 구독자 최대 Guest는 1명`() {
        assertEquals(1, FriendInviteSystem.MONTHLY_MAX_GUESTS)
    }

    // ===== 상수 일관성 테스트 =====

    @Test
    fun `SubscriptionModel과 FriendInviteSystem 상수 일치`() {
        assertEquals(
            SubscriptionModel.YEARLY_INVITE_COUNT,
            FriendInviteSystem.YEARLY_MAX_GUESTS
        )
        assertEquals(
            SubscriptionModel.MONTHLY_INVITE_COUNT,
            FriendInviteSystem.MONTHLY_MAX_GUESTS
        )
        assertEquals(
            SubscriptionModel.INVITE_DURATION_DAYS,
            FriendInviteSystem.GUEST_DURATION_DAYS
        )
    }
}
