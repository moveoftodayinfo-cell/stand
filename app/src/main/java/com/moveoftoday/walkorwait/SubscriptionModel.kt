package com.moveoftoday.walkorwait

/**
 * Stand 결제 모델 (심플 시스템)
 *
 * - 매달 4,700원 구독 결제 (필수)
 * - 95% 이상 달성 시: 친구 초대 쿠폰 1장 획득
 * - 친구는 쿠폰으로 1달 무료 이용
 *
 * 가격 기준: 염창역 스타벅스 아이스 아메리카노 (가격 변동 시 조정)
 *
 * 사용자 타입:
 * - PAID: 4,700원 결제한 사용자 (95% 달성 시 친구 쿠폰 획득)
 * - GUEST: 친구 초대로 1달 무료 이용 중
 */
object SubscriptionModel {

    // 결제 가격 (염창역 스타벅스 아이스 아메리카노 기준)
    const val MONTHLY_PRICE = 4700        // 월 결제 금액

    // 달성률 기준
    const val THRESHOLD_COUPON = 95       // 친구 쿠폰 획득 기준

    /**
     * 친구 초대 쿠폰 획득 여부 (95% 이상)
     */
    fun earnsFriendCoupon(achievementRate: Float): Boolean {
        return achievementRate >= THRESHOLD_COUPON
    }

    /**
     * 달성률에 따른 상태 텍스트
     */
    fun getStatusText(achievementRate: Float): String {
        return if (achievementRate >= THRESHOLD_COUPON) {
            "친구 초대 쿠폰 획득!"
        } else {
            "95% 달성 시 친구 쿠폰"
        }
    }

    /**
     * 달성률에 따른 상태 아이콘 이름
     */
    fun getStatusIconName(achievementRate: Float): String {
        return if (achievementRate >= THRESHOLD_COUPON) "icon_trophy" else "icon_target"
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
     * 사용자 타입 enum
     */
    enum class UserType {
        PAID,       // 결제 사용자
        GUEST       // 친구 초대 무료
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
