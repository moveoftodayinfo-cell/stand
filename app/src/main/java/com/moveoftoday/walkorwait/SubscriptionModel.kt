package com.moveoftoday.walkorwait

/**
 * rebon 구독 모델
 *
 * 구독 가격:
 * - 월간: 3,900원/월 (3일 무료 체험)
 * - 연간: 39,000원/년 (3일 무료 체험, 2개월 무료)
 *
 * 보상 시스템:
 * - 일일 목표 달성 시: 방어 티켓 획득 (연속일수 보호용)
 *
 * 친구 초대:
 * - 연간 구독자: 12명 초대 가능 (각 30일 무료)
 * - 월간 구독자: 1명 초대 가능 (30일 무료)
 */
object SubscriptionModel {

    // 결제 가격
    const val MONTHLY_PRICE = 3900        // 월간 구독
    const val YEARLY_PRICE = 39000        // 연간 구독 (2개월 무료)

    // 친구 초대 설정
    const val YEARLY_INVITE_COUNT = 12    // 연간 구독자 초대 가능 수
    const val MONTHLY_INVITE_COUNT = 1    // 월간 구독자 초대 가능 수
    const val INVITE_DURATION_DAYS = 30   // 초대받은 사람 무료 기간

    /**
     * 구독 타입에 따른 초대 가능 수
     */
    fun getInviteCount(isYearly: Boolean): Int {
        return if (isYearly) YEARLY_INVITE_COUNT else MONTHLY_INVITE_COUNT
    }

    /**
     * 연간 구독 시 절약 금액 계산
     */
    fun getYearlySavings(): Int {
        return (MONTHLY_PRICE * 12) - YEARLY_PRICE  // 7,800원 절약
    }

    /**
     * 가격 포맷팅
     */
    fun formatPrice(price: Int): String {
        return when (price) {
            0 -> "무료"
            else -> "${String.format("%,d", price)}원"
        }
    }

    /**
     * 구독 타입 enum
     */
    enum class SubscriptionType {
        MONTHLY,    // 월간 구독
        YEARLY,     // 연간 구독
        PROMO,      // 프로모 코드
        GUEST       // 친구 초대 무료
    }
}

/**
 * 친구 초대 시스템
 *
 * - 연간 구독자: 12명 초대 가능
 * - 월간 구독자: 1명 초대 가능
 * - 초대받은 사용자: 30일 무료 이용
 */
object FriendInviteSystem {
    const val GUEST_DURATION_DAYS = 30   // Guest 무료 사용 기간 (30일)
    const val YEARLY_MAX_GUESTS = 12     // 연간 구독자 최대 Guest 수
    const val MONTHLY_MAX_GUESTS = 1     // 월간 구독자 최대 Guest 수
}
