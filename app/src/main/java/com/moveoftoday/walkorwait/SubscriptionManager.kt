package com.moveoftoday.walkorwait

import android.content.Context
import android.util.Log
import com.android.billingclient.api.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Stand 결제 데이터
 *
 * 사용자 타입:
 * - PAID: 구독 결제한 사용자 (월간 3,900원 또는 연간 39,000원)
 * - GUEST: 친구 초대로 1달 무료 이용 중
 *
 * 심플 시스템:
 * - 월간 구독: 3,900원/월 (3일 무료 체험)
 * - 연간 구독: 39,000원/년 (3일 무료 체험, 2개월 무료)
 * - 달성률에 따라 streak 방어 티켓 지급 (90%=1장, 95%=2장, 100%=3장)
 */
data class SubscriptionData(
    val monthId: String = "",
    val isPaid: Boolean = false,
    val isActive: Boolean = false,
    val userType: String = "PAID", // PAID, GUEST
    val price: Int = SubscriptionModel.MONTHLY_PRICE,
    val purchaseToken: String? = null,
    val orderId: String? = null,
    val totalDays: Int = 0,
    val successDays: Int = 0,
    val achievementRate: Float = 0f,
    val earnedFriendCoupon: Boolean = false, // 이번 달 친구 쿠폰 획득 여부
    val availableFriendCoupons: Int = 0, // 사용 가능한 친구 쿠폰 수
    val consecutiveSuccessCount: Int = 0, // 연속 성공 횟수
    val goal: Int = 8000,
    val controlDays: List<Int> = emptyList(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    // 기본 초대 코드 (정기결제 시 발급)
    val inviteCode: String? = null,
    val maxInvites: Int = 1, // 월간: 1명, 연간: 12명
    val inviteGuests: List<Map<String, Any>> = emptyList(), // 초대된 게스트 목록
    val inviteGuestId: String? = null, // 하위 호환용 (첫 번째 게스트)
    val inviteGuestEmail: String? = null,
    val inviteGuestUsedAt: Date? = null,
    // 보너스 초대 코드 (95% 달성 시 발급)
    val bonusInviteCode: String? = null,
    val bonusGuestId: String? = null,
    val bonusGuestEmail: String? = null,
    val bonusGuestUsedAt: Date? = null,
    // Guest 관련
    val hostId: String? = null, // Guest인 경우 Host의 userId
    val guestId: String? = null, // (deprecated) 이전 버전 호환용
    val guestExpiresAt: Date? = null, // Guest 만료 시간
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

class SubscriptionManager(private val context: Context) {
    private val TAG = "SubscriptionManager"
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private fun getMonthId(date: Date = Date()): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return sdf.format(date)
        }

        private fun getNextMonthId(currentMonthId: String): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(currentMonthId) ?: Date()
            calendar.add(Calendar.MONTH, 1)
            return sdf.format(calendar.time)
        }

        private fun getPreviousMonthId(currentMonthId: String): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(currentMonthId) ?: Date()
            calendar.add(Calendar.MONTH, -1)
            return sdf.format(calendar.time)
        }
    }

    /**
     * 현재 사용자 ID 가져오기
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 신규 구독 생성 (결제 완료 후 - Host용)
     *
     * @param goal 일일 목표 걸음수
     * @param controlDays 제어할 요일 (1=월요일 ~ 7=일요일)
     * @param purchase Google Play 구매 정보
     * @param isYearly 연간 구독 여부 (월간: false, 연간: true)
     */
    suspend fun createSubscription(
        goal: Int,
        controlDays: List<Int>,
        purchase: Purchase,
        isYearly: Boolean = false
    ): Result<SubscriptionData> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        val monthId = getMonthId()

        try {
            val calendar = Calendar.getInstance()
            val startDate = calendar.time

            // 초대 코드 만료일 계산 (결제일 기준 1달 후)
            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.time

            // 친구 초대 코드 생성 (매달 새로운 코드)
            val inviteCode = generateInviteCode(userId, monthId)
            val bonusInviteCode = generateBonusInviteCode(userId, monthId)

            // 구독 타입에 따른 초대 횟수 설정 (월간: 1명, 연간: 12명)
            val maxInvites = if (isYearly) 12 else 1

            val subscription = SubscriptionData(
                monthId = monthId,
                isPaid = true,
                isActive = true,
                userType = "PAID",
                price = if (isYearly) 39000 else SubscriptionModel.MONTHLY_PRICE,
                purchaseToken = purchase.purchaseToken,
                orderId = purchase.orderId,
                totalDays = 0,
                successDays = 0,
                achievementRate = 0f,
                earnedFriendCoupon = false,
                availableFriendCoupons = 0,
                consecutiveSuccessCount = 0,
                goal = goal,
                controlDays = controlDays,
                startDate = startDate,
                endDate = endDate,
                inviteCode = inviteCode,
                maxInvites = maxInvites,
                inviteGuests = emptyList(),
                inviteGuestId = null,
                inviteGuestEmail = null,
                inviteGuestUsedAt = null,
                bonusInviteCode = bonusInviteCode,
                bonusGuestId = null,
                bonusGuestEmail = null,
                bonusGuestUsedAt = null,
                hostId = null,
                guestId = null,
                guestExpiresAt = null,
                createdAt = Date(),
                updatedAt = Date()
            )

            db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(monthId)
                .set(subscription.toFirestoreMap())
                .await()

            // 대시보드 추적용 사용자 문서 생성
            createUserDocument(userId, goal)

            // Analytics: 초대 코드 생성 추적
            AnalyticsManager.trackInviteCodeGenerated()

            Log.d(TAG, "✅ Subscription created: $monthId, inviteCode=$inviteCode")
            return Result.success(subscription)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create subscription: ${e.message}")
            return Result.failure(e)
        }
    }

    /**
     * 기본 초대 코드 생성 (정기결제 시 발급)
     * 형식: REBON-{userId앞3자}{monthId해시4자}
     */
    private fun generateInviteCode(userId: String, monthId: String): String {
        val userPart = userId.take(3).uppercase()
        val monthHash = (userId + monthId).hashCode().toString(16).takeLast(4).uppercase()
        return "REBON-$userPart$monthHash"
    }

    /**
     * 보너스 초대 코드 생성 (95% 달성 시 활성화)
     * 형식: BONUS-{userId앞3자}{monthId해시4자}
     */
    private fun generateBonusInviteCode(userId: String, monthId: String): String {
        val userPart = userId.take(3).uppercase()
        val monthHash = (userId + monthId + "bonus").hashCode().toString(16).takeLast(4).uppercase()
        return "BONUS-$userPart$monthHash"
    }

    /**
     * 대시보드 추적용 사용자 문서 생성 (결제 시 호출)
     */
    private fun createUserDocument(userId: String, goal: Int) {
        val userEmail = auth.currentUser?.email ?: ""
        val now = System.currentTimeMillis()

        // 부모 문서 생성/업데이트
        val userDoc = hashMapOf(
            "email" to userEmail,
            "lastActiveAt" to now,
            "lastUpdated" to now,
            "createdAt" to now
        )
        db.collection("users")
            .document(userId)
            .set(userDoc, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User document created for paid user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to create user document: ${e.message}")
            }

        // settings 서브컬렉션 생성
        val settingsDoc = hashMapOf(
            "lastActiveAt" to now,
            "paidDeposit" to true,  // 결제 사용자
            "goal" to goal
        )
        db.collection("users")
            .document(userId)
            .collection("userData")
            .document("settings")
            .set(settingsDoc, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Settings document created for paid user: $userId")
            }
    }

    /**
     * 친구 초대 (Host가 친구에게 코드 공유 후 친구가 입력)
     * 친구는 1달간 무료 사용 가능, Host 구독 종료 시 같이 종료
     *
     * @param inviteCode 초대 코드
     * @param goal 일일 목표 걸음수
     * @param controlDays 제어할 요일
     */
    suspend fun joinAsGuest(
        inviteCode: String,
        goal: Int,
        controlDays: List<Int>
    ): Result<SubscriptionData> {
        val guestUserId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        val monthId = getMonthId()

        try {
            // 초대 코드로 Host 찾기
            val hostSnapshot = db.collectionGroup("subscriptions")
                .whereEqualTo("inviteCode", inviteCode)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            if (hostSnapshot.isEmpty) {
                return Result.failure(Exception("유효하지 않은 초대 코드입니다"))
            }

            val hostDoc = hostSnapshot.documents.first()
            val hostPath = hostDoc.reference.path
            val hostId = hostPath.split("/")[1] // users/{userId}/subscriptions/...

            // Host가 이미 친구를 초대했는지 확인
            val hostSubscription = hostDoc.toObject(SubscriptionData::class.java)
            if (hostSubscription?.guestId != null) {
                return Result.failure(Exception("이미 초대된 친구가 있습니다"))
            }

            // 자기 자신 초대 방지
            if (hostId == guestUserId) {
                return Result.failure(Exception("자신의 코드는 사용할 수 없습니다"))
            }

            val calendar = Calendar.getInstance()
            val startDate = calendar.time

            // 1달 후 만료
            calendar.add(Calendar.MONTH, 1)
            val guestExpiresAt = calendar.time

            // Guest 구독 생성
            val guestSubscription = SubscriptionData(
                monthId = monthId,
                isPaid = false, // Guest는 무료
                isActive = true,
                userType = "GUEST",
                price = 0,
                purchaseToken = null,
                orderId = null,
                totalDays = 0,
                successDays = 0,
                achievementRate = 0f,
                earnedFriendCoupon = false,
                availableFriendCoupons = 0,
                consecutiveSuccessCount = 0,
                goal = goal,
                controlDays = controlDays,
                startDate = startDate,
                endDate = guestExpiresAt,
                inviteCode = null,
                hostId = hostId,
                guestId = null,
                guestExpiresAt = guestExpiresAt,
                createdAt = Date(),
                updatedAt = Date()
            )

            // Guest 구독 저장
            db.collection("users")
                .document(guestUserId)
                .collection("subscriptions")
                .document(monthId)
                .set(guestSubscription.toFirestoreMap())
                .await()

            // Host의 guestId 업데이트
            hostDoc.reference.update(
                mapOf(
                    "guestId" to guestUserId,
                    "updatedAt" to Date()
                )
            ).await()

            Log.d(TAG, "✅ Guest subscription created: guestId=$guestUserId, hostId=$hostId")
            return Result.success(guestSubscription)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to join as guest: ${e.message}")
            return Result.failure(e)
        }
    }

    /**
     * Guest 구독이 유효한지 확인
     * Host 구독이 종료되었거나 만료되면 false 반환
     */
    suspend fun isGuestSubscriptionValid(): Boolean {
        val userId = getCurrentUserId() ?: return false
        val monthId = getMonthId()

        try {
            val subscription = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(monthId)
                .get()
                .await()
                .toObject(SubscriptionData::class.java)
                ?: return false

            // PAID 구독자는 항상 유효 (Google Play 구독 상태에 따름)
            if (subscription.userType == "PAID") {
                return subscription.isActive
            }

            // GUEST인 경우
            if (subscription.userType == "GUEST") {
                val hostId = subscription.hostId ?: return false

                // 만료 시간 확인
                val expiresAt = subscription.guestExpiresAt
                if (expiresAt != null && Date().after(expiresAt)) {
                    // 만료됨 - 비활성화
                    deactivateGuestSubscription(userId, monthId)
                    return false
                }

                // Host 구독 상태 확인
                val hostSubscription = db.collection("users")
                    .document(hostId)
                    .collection("subscriptions")
                    .document(monthId)
                    .get()
                    .await()
                    .toObject(SubscriptionData::class.java)

                if (hostSubscription == null || !hostSubscription.isActive) {
                    // Host 구독 종료 - Guest도 비활성화
                    deactivateGuestSubscription(userId, monthId)
                    return false
                }

                return true
            }

            return false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to check guest subscription: ${e.message}")
            return false
        }
    }

    /**
     * Guest 구독 비활성화
     */
    private suspend fun deactivateGuestSubscription(userId: String, monthId: String) {
        try {
            db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(monthId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Log.d(TAG, "✅ Guest subscription deactivated: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to deactivate guest subscription: ${e.message}")
        }
    }

    /**
     * Host 구독 종료 시 Guest도 종료
     */
    suspend fun onHostSubscriptionEnded() {
        val userId = getCurrentUserId() ?: return
        val monthId = getMonthId()

        try {
            val subscription = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(monthId)
                .get()
                .await()
                .toObject(SubscriptionData::class.java)
                ?: return

            val guestId = subscription.guestId ?: return

            // Guest 구독 비활성화
            deactivateGuestSubscription(guestId, monthId)

            Log.d(TAG, "✅ Guest subscription ended due to host cancellation: guestId=$guestId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to end guest subscription: ${e.message}")
        }
    }

    /**
     * 현재 월 구독 정보 가져오기
     */
    suspend fun getCurrentSubscription(): SubscriptionData? {
        val userId = getCurrentUserId() ?: return null
        val monthId = getMonthId()

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(monthId)
                .get()
                .await()

            snapshot.toObject(SubscriptionData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get current subscription: ${e.message}")
            null
        }
    }

    /**
     * 월말 정산 (심플 시스템)
     *
     * 🛡️ 90% 이상 → streak 방어 티켓 1장
     * 🛡️ 95% 이상 → streak 방어 티켓 2장
     * 🛡️ 100% 달성 → streak 방어 티켓 3장
     * ❌ 90% 미만 → 티켓 없음
     */
    suspend fun processMonthlyResult(
        currentMonthId: String,
        totalDays: Int,
        successDays: Int
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))

        try {
            val achievementRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f
            val achievementRateDecimal = if (totalDays > 0) (successDays.toFloat() / totalDays) else 0f
            // 친구 쿠폰 시스템 비활성화 (현재는 방어 티켓 시스템으로 대체됨)
            val earnedCoupon = false

            // Streak 방어 티켓 지급 (로컬 저장)
            val preferenceManager = PreferenceManager(context)
            val ticketsAwarded = preferenceManager.awardStreakDefenseTickets(achievementRateDecimal)
            if (ticketsAwarded > 0) {
                Log.d(TAG, "🛡️ Awarded $ticketsAwarded streak defense ticket(s)")
            }

            // 현재 월 구독 정보 가져오기
            val currentSubscription = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(currentMonthId)
                .get()
                .await()
                .toObject(SubscriptionData::class.java)
                ?: return Result.failure(Exception("Current subscription not found"))

            // 쿠폰 획득 시 누적
            val newCouponCount = if (earnedCoupon) {
                currentSubscription.availableFriendCoupons + 1
            } else {
                currentSubscription.availableFriendCoupons
            }

            // 연속 성공 카운트 (95% 이상만)
            val consecutiveCount = if (earnedCoupon) {
                currentSubscription.consecutiveSuccessCount + 1
            } else {
                0
            }

            // 현재 월 업데이트
            db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(currentMonthId)
                .update(
                    mapOf(
                        "totalDays" to totalDays,
                        "successDays" to successDays,
                        "achievementRate" to achievementRate,
                        "earnedFriendCoupon" to earnedCoupon,
                        "availableFriendCoupons" to newCouponCount,
                        "consecutiveSuccessCount" to consecutiveCount,
                        "ticketsAwarded" to ticketsAwarded,
                        "updatedAt" to Date()
                    )
                )
                .await()

            Log.d(TAG, "✅ Monthly result: rate=${achievementRate.toInt()}%, earnedCoupon=$earnedCoupon, totalCoupons=$newCouponCount, consecutive=$consecutiveCount, tickets=$ticketsAwarded")

            return Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to process monthly result: ${e.message}")
            return Result.failure(e)
        }
    }

    /**
     * 일일 걸음 수 기록 저장
     */
    suspend fun saveDailyRecord(
        dateId: String,
        steps: Int,
        goal: Int,
        isSuccess: Boolean,
        isControlDay: Boolean
    ) {
        val userId = getCurrentUserId() ?: return

        try {
            val record = mapOf(
                "dateId" to dateId,
                "steps" to steps,
                "goal" to goal,
                "isSuccess" to isSuccess,
                "isControlDay" to isControlDay,
                "date" to Date()
            )

            db.collection("users")
                .document(userId)
                .collection("dailyRecords")
                .document(dateId)
                .set(record)
                .await()

            Log.d(TAG, "✅ Daily record saved: $dateId, steps=$steps, isSuccess=$isSuccess")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save daily record: ${e.message}")
        }
    }

    /**
     * SubscriptionData를 Firestore Map으로 변환
     *
     * CRITICAL: Firestore SDK는 Kotlin data class의 Boolean 필드를 제대로 직렬화하지 못함
     * 특히 isActive, isPaid 같은 필드가 누락될 수 있음
     * 반드시 명시적 Map으로 변환하여 저장해야 함
     */
    private fun SubscriptionData.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "monthId" to monthId,
            "isPaid" to isPaid,
            "isActive" to isActive,
            "userType" to userType,
            "price" to price,
            "purchaseToken" to purchaseToken,
            "orderId" to orderId,
            "totalDays" to totalDays,
            "successDays" to successDays,
            "achievementRate" to achievementRate,
            "earnedFriendCoupon" to earnedFriendCoupon,
            "availableFriendCoupons" to availableFriendCoupons,
            "consecutiveSuccessCount" to consecutiveSuccessCount,
            "goal" to goal,
            "controlDays" to controlDays,
            "startDate" to startDate,
            "endDate" to endDate,
            "inviteCode" to inviteCode,
            "maxInvites" to maxInvites,
            "inviteGuests" to inviteGuests,
            "inviteGuestId" to inviteGuestId,
            "inviteGuestEmail" to inviteGuestEmail,
            "inviteGuestUsedAt" to inviteGuestUsedAt,
            "bonusInviteCode" to bonusInviteCode,
            "bonusGuestId" to bonusGuestId,
            "bonusGuestEmail" to bonusGuestEmail,
            "bonusGuestUsedAt" to bonusGuestUsedAt,
            "hostId" to hostId,
            "guestId" to guestId,
            "guestExpiresAt" to guestExpiresAt,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        ).filterValues { it != null }
    }

    /**
     * 월별 성공 일수 계산
     */
    suspend fun calculateMonthlySuccess(monthId: String): Pair<Int, Int>? {
        val userId = getCurrentUserId() ?: return null

        try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val monthDate = sdf.parse(monthId) ?: return null

            val calendar = Calendar.getInstance()
            calendar.time = monthDate
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1

            // 해당 월의 모든 일일 기록 가져오기
            val records = db.collection("users")
                .document(userId)
                .collection("dailyRecords")
                .whereGreaterThanOrEqualTo("dateId", "$year-${month.toString().padStart(2, '0')}-01")
                .whereLessThan("dateId", getNextMonthId(monthId) + "-01")
                .get()
                .await()

            var totalDays = 0
            var successDays = 0

            for (doc in records) {
                val isControlDay = doc.getBoolean("isControlDay") ?: false
                val isSuccess = doc.getBoolean("isSuccess") ?: false

                if (isControlDay) {
                    totalDays++
                    if (isSuccess) {
                        successDays++
                    }
                }
            }

            return Pair(totalDays, successDays)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to calculate monthly success: ${e.message}")
            return null
        }
    }
}
