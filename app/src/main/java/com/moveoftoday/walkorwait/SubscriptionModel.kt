package com.moveoftoday.walkorwait

/**
 * Stand 구독 모델
 *
 * - 정가: 월 4,900원 (Google Play 고정 결제)
 * - 미션 달성률에 따라 Stand 크레딧 지급
 * - 크레딧으로 실질 구독료 할인 효과
 *
 * 크레딧 시스템:
 * - 95% 이상: +4,900 크레딧 (실질 무료)
 * - 80-94%: +2,400 크레딧 (실질 2,500원)
 * - 80% 미만: 크레딧 없음 (실질 4,900원)
 */
object SubscriptionModel {

    // 구독 가격 (Google Play 고정)
    const val BASE_PRICE = 4900           // 정가

    // 실질 부담 금액 (크레딧 적용 후)
    const val EFFECTIVE_PRICE_FREE = 0         // 95% 이상: 실질 무료
    const val EFFECTIVE_PRICE_DISCOUNT = 2500  // 80-94%: 실질 2,500원
    const val EFFECTIVE_PRICE_PENALTY = 4900   // 80% 미만: 정가 그대로

    // 크레딧 지급량
    const val CREDIT_TIER_FREE = 4900     // 95% 이상: 전액 크레딧
    const val CREDIT_TIER_DISCOUNT = 2400 // 80-94%: 2,400 크레딧 (4900-2500)
    const val CREDIT_TIER_PENALTY = 0     // 80% 미만: 크레딧 없음

    // 달성률 기준
    const val THRESHOLD_FREE = 95         // 무료 기준
    const val THRESHOLD_DISCOUNT = 80     // 할인 기준

    /**
     * 달성률에 따른 크레딧 지급량 계산
     */
    fun getCreditAmount(achievementRate: Float): Int {
        return when {
            achievementRate >= THRESHOLD_FREE -> CREDIT_TIER_FREE
            achievementRate >= THRESHOLD_DISCOUNT -> CREDIT_TIER_DISCOUNT
            else -> CREDIT_TIER_PENALTY
        }
    }

    /**
     * 달성률에 따른 실질 부담 금액 계산
     */
    fun getEffectivePrice(achievementRate: Float): Int {
        return when {
            achievementRate >= THRESHOLD_FREE -> EFFECTIVE_PRICE_FREE
            achievementRate >= THRESHOLD_DISCOUNT -> EFFECTIVE_PRICE_DISCOUNT
            else -> EFFECTIVE_PRICE_PENALTY
        }
    }

    /**
     * 달성률에 따른 다음 달 구독료 계산 (하위 호환용)
     */
    fun getNextMonthPrice(achievementRate: Float): Int {
        return getEffectivePrice(achievementRate)
    }

    /**
     * 달성률에 따른 할인 금액 계산
     */
    fun getDiscountAmount(achievementRate: Float): Int {
        return BASE_PRICE - getEffectivePrice(achievementRate)
    }

    /**
     * 달성률에 따른 상태 텍스트
     */
    fun getStatusText(achievementRate: Float): String {
        return when {
            achievementRate >= THRESHOLD_FREE -> "다음 달 무료!"
            achievementRate >= THRESHOLD_DISCOUNT -> "다음 달 2,500원"
            else -> "다음 달 4,900원"
        }
    }

    /**
     * 크레딧 지급 상태 텍스트
     */
    fun getCreditStatusText(achievementRate: Float): String {
        val credit = getCreditAmount(achievementRate)
        return when {
            credit > 0 -> "+${String.format("%,d", credit)} 크레딧"
            credit < 0 -> "${String.format("%,d", credit)} 크레딧"
            else -> "0 크레딧"
        }
    }

    /**
     * 달성률에 따른 상태 이모지
     */
    fun getStatusEmoji(achievementRate: Float): String {
        return when {
            achievementRate >= THRESHOLD_FREE -> "🎉"
            achievementRate >= THRESHOLD_DISCOUNT -> "✨"
            else -> "💪"
        }
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
     * 크레딧 포맷팅
     */
    fun formatCredit(credit: Int): String {
        return when {
            credit > 0 -> "+${String.format("%,d", credit)}"
            else -> String.format("%,d", credit)
        }
    }

    /**
     * 구독 티어 enum
     */
    enum class Tier {
        FREE,       // 95% 이상 - 실질 0원
        DISCOUNT,   // 80-94% - 실질 2,500원
        PENALTY     // 80% 미만 - 정가 4,900원
    }

    fun getTier(achievementRate: Float): Tier {
        return when {
            achievementRate >= THRESHOLD_FREE -> Tier.FREE
            achievementRate >= THRESHOLD_DISCOUNT -> Tier.DISCOUNT
            else -> Tier.PENALTY
        }
    }
}

/**
 * 친구 초대 시스템 (결제자 전용)
 *
 * - 결제자(Host)만 친구 1명 초대 가능
 * - 친구(Guest)는 1달간 무료 사용
 * - Host 구독 종료 시 Guest도 종료
 */
object FriendInviteSystem {
    const val GUEST_DURATION_MONTHS = 1  // Guest 무료 사용 기간
    const val MAX_GUESTS_PER_HOST = 1    // Host당 최대 Guest 수
}
