package com.moveoftoday.walkorwait

import android.content.Context
import android.util.Log
import com.android.billingclient.api.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Stand 구독 데이터
 *
 * 구독 타입:
 * - PAID: 직접 결제한 사용자 (Host)
 * - GUEST: 결제자의 친구 (1달 무료, Host 구독 종료 시 같이 종료)
 *
 * 크레딧 시스템 (PAID 전용):
 * - Google Play 구독: 4,900원/월 고정
 * - 달성률에 따라 Stand 크레딧 지급
 * - 95% 이상: +4,900 크레딧 (실질 무료)
 * - 80~94%: +2,400 크레딧 (실질 2,500원)
 * - 80% 미만: 크레딧 없음 (정가 4,900원)
 */
data class SubscriptionData(
    val monthId: String = "",
    val isPaid: Boolean = false,
    val isActive: Boolean = false,
    val subscriptionType: String = "PAID", // PAID or GUEST
    val basePrice: Int = SubscriptionModel.BASE_PRICE,
    val purchaseToken: String? = null,
    val orderId: String? = null,
    val totalDays: Int = 0,
    val successDays: Int = 0,
    val achievementRate: Float = 0f,
    val tier: String = "PENALTY", // FREE, DISCOUNT, PENALTY
    val creditEarned: Int = 0, // 이번 달 획득 크레딧
    val creditBalance: Int = 0, // 누적 크레딧 잔액
    val effectivePrice: Int = SubscriptionModel.BASE_PRICE, // 실질 부담 금액
    val consecutiveSuccessCount: Int = 0,
    val goal: Int = 8000,
    val controlDays: List<Int> = emptyList(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    val inviteCode: String? = null, // 친구 초대용 코드 (Host만)
    val hostId: String? = null, // Guest인 경우 Host의 userId
    val guestId: String? = null, // Host가 초대한 친구의 userId
    val guestExpiresAt: Date? = null, // Guest 구독 만료 시간
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
     */
    suspend fun createSubscription(
        goal: Int,
        controlDays: List<Int>,
        purchase: Purchase
    ): Result<SubscriptionData> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        val monthId = getMonthId()

        try {
            val calendar = Calendar.getInstance()
            val startDate = calendar.time

            // 월말 계산
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = calendar.time

            // 친구 초대 코드 생성
            val inviteCode = generateInviteCode(userId)

            val subscription = SubscriptionData(
                monthId = monthId,
                isPaid = true,
                isActive = true,
                subscriptionType = "PAID",
                basePrice = SubscriptionModel.BASE_PRICE,
                purchaseToken = purchase.purchaseToken,
                orderId = purchase.orderId,
                totalDays = 0,
                successDays = 0,
                achievementRate = 0f,
                tier = "PENALTY",
                creditEarned = 0,
                creditBalance = 0,
                effectivePrice = SubscriptionModel.BASE_PRICE,
                consecutiveSuccessCount = 0,
                goal = goal,
                controlDays = controlDays,
                startDate = startDate,
                endDate = endDate,
                inviteCode = inviteCode,
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
                .set(subscription)
                .await()

            Log.d(TAG, "✅ Subscription created: $monthId, inviteCode=$inviteCode")
            return Result.success(subscription)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create subscription: ${e.message}")
            return Result.failure(e)
        }
    }

    /**
     * 친구 초대 코드 생성
     */
    private fun generateInviteCode(userId: String): String {
        return "STAND-${userId.take(6).uppercase()}"
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
                subscriptionType = "GUEST",
                basePrice = 0,
                purchaseToken = null,
                orderId = null,
                totalDays = 0,
                successDays = 0,
                achievementRate = 0f,
                tier = "PENALTY",
                creditEarned = 0,
                creditBalance = 0,
                effectivePrice = 0,
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
                .set(guestSubscription)
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
            if (subscription.subscriptionType == "PAID") {
                return subscription.isActive
            }

            // GUEST인 경우
            if (subscription.subscriptionType == "GUEST") {
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
     * 월말 정산 및 크레딧 지급
     *
     * 크레딧 시스템:
     * 🏆 95% 이상 → +4,900 크레딧 (실질 무료)
     * ✅ 80~94% → +2,400 크레딧 (실질 2,500원)
     * ❌ 80% 미만 → 크레딧 없음 (정가 4,900원)
     */
    suspend fun processMonthlyResult(
        currentMonthId: String,
        totalDays: Int,
        successDays: Int
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))

        try {
            val achievementRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f

            // 티어 판정 및 크레딧 계산
            val tier = SubscriptionModel.getTier(achievementRate)
            val creditEarned = SubscriptionModel.getCreditAmount(achievementRate)
            val effectivePrice = SubscriptionModel.getEffectivePrice(achievementRate)

            // 현재 월 구독 정보 가져오기 (크레딧 잔액 확인용)
            val currentSubscription = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(currentMonthId)
                .get()
                .await()
                .toObject(SubscriptionData::class.java)
                ?: return Result.failure(Exception("Current subscription not found"))

            // 새 크레딧 잔액 계산
            val newCreditBalance = (currentSubscription.creditBalance + creditEarned).coerceAtLeast(0)

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
                        "tier" to tier.name,
                        "creditEarned" to creditEarned,
                        "creditBalance" to newCreditBalance,
                        "effectivePrice" to effectivePrice,
                        "updatedAt" to Date()
                    )
                )
                .await()

            // 다음달 구독 생성
            val nextMonthId = getNextMonthId(currentMonthId)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val nextStartDate = calendar.time
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val nextEndDate = calendar.time

            // 연속 성공 카운트 (95% 이상만)
            val consecutiveCount = if (tier == SubscriptionModel.Tier.FREE) {
                currentSubscription.consecutiveSuccessCount + 1
            } else {
                0
            }

            val nextSubscription = SubscriptionData(
                monthId = nextMonthId,
                isPaid = true, // Google Play에서 자동 결제
                isActive = true,
                subscriptionType = currentSubscription.subscriptionType,
                basePrice = SubscriptionModel.BASE_PRICE,
                purchaseToken = currentSubscription.purchaseToken,
                orderId = null,
                totalDays = 0,
                successDays = 0,
                achievementRate = 0f,
                tier = "PENALTY", // 다음 달 티어는 다음 달 정산 시 결정
                creditEarned = 0,
                creditBalance = newCreditBalance, // 이전 달 잔액 이월
                effectivePrice = SubscriptionModel.BASE_PRICE, // 다음 달 정산 시 결정
                consecutiveSuccessCount = consecutiveCount,
                goal = currentSubscription.goal,
                controlDays = currentSubscription.controlDays,
                startDate = nextStartDate,
                endDate = nextEndDate,
                inviteCode = currentSubscription.inviteCode,
                hostId = currentSubscription.hostId,
                guestId = null, // Guest는 매월 새로 초대 필요
                guestExpiresAt = null,
                createdAt = Date(),
                updatedAt = Date()
            )

            db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(nextMonthId)
                .set(nextSubscription)
                .await()

            Log.d(TAG, "✅ Monthly result: rate=${achievementRate.toInt()}%, tier=${tier.name}, credit=$creditEarned, balance=$newCreditBalance, consecutive=$consecutiveCount")

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
