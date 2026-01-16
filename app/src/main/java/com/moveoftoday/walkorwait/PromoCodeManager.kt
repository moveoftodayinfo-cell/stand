package com.moveoftoday.walkorwait

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * 프로모션 코드 관리
 *
 * 코드 종류:
 * 1. 친구 초대 코드 (STAND-XXXXXX): 1달 무료
 * 2. 이벤트 코드 (EVENT-XXXXXX): 특별 혜택
 * 3. 테스트 코드 (TEST-FREE): 테스트용
 */
class PromoCodeManager(private val context: Context) {
    private val TAG = "PromoCodeManager"
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val preferenceManager = PreferenceManager(context)

    sealed class PromoResult {
        data class Success(
            val type: PromoType,
            val message: String,
            val freeDays: Int = 0,
            val discount: Int = 0
        ) : PromoResult()

        data class Error(val message: String) : PromoResult()
    }

    enum class PromoType {
        FRIEND_INVITE,  // 친구 초대: 1달 무료
        EVENT_CODE,     // 이벤트: 다양한 혜택
        TEST_CODE       // 테스트용
    }

    /**
     * 프로모션 코드 검증 및 적용
     */
    suspend fun validateAndApply(code: String): PromoResult {
        val trimmedCode = code.trim().uppercase()

        if (trimmedCode.isEmpty()) {
            return PromoResult.Error("코드를 입력해주세요")
        }

        // 이미 사용한 코드인지 확인
        if (preferenceManager.isPromoCodeUsed(trimmedCode)) {
            return PromoResult.Error("이미 사용한 코드입니다")
        }

        return when {
            trimmedCode.startsWith("STAND-") -> validateFriendInviteCode(trimmedCode)
            trimmedCode.startsWith("EVENT-") -> validateEventCode(trimmedCode)
            trimmedCode == "TEST-FREE" -> applyTestCode()
            trimmedCode == "STANDFREE" -> applyStandFreeCode()
            else -> PromoResult.Error("유효하지 않은 코드입니다")
        }
    }

    /**
     * 친구 초대 코드 검증 (STAND-XXXXXX)
     */
    private suspend fun validateFriendInviteCode(code: String): PromoResult {
        val currentUserId = auth.currentUser?.uid
            ?: return PromoResult.Error("로그인이 필요합니다")

        try {
            // Firebase에서 초대 코드 검색
            val snapshot = db.collectionGroup("subscriptions")
                .whereEqualTo("inviteCode", code)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return PromoResult.Error("유효하지 않은 초대 코드입니다")
            }

            val hostDoc = snapshot.documents.first()
            val hostPath = hostDoc.reference.path
            val hostId = hostPath.split("/")[1]

            // 자기 자신 초대 방지
            if (hostId == currentUserId) {
                return PromoResult.Error("자신의 코드는 사용할 수 없습니다")
            }

            // 이미 게스트가 있는지 확인
            val guestId = hostDoc.getString("guestId")
            if (guestId != null) {
                return PromoResult.Error("이미 사용된 초대 코드입니다")
            }

            // 코드 사용 기록 저장
            preferenceManager.saveUsedPromoCode(code)
            preferenceManager.savePromoCodeType("FRIEND_INVITE")
            preferenceManager.savePromoHostId(hostId)

            return PromoResult.Success(
                type = PromoType.FRIEND_INVITE,
                message = "친구 초대 코드가 적용되었습니다!\n1달간 무료로 사용하세요",
                freeDays = 30
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to validate invite code: ${e.message}")
            return PromoResult.Error("코드 확인 중 오류가 발생했습니다")
        }
    }

    /**
     * 이벤트 코드 검증 (EVENT-XXXXXX)
     */
    private suspend fun validateEventCode(code: String): PromoResult {
        try {
            // Firebase에서 이벤트 코드 검색
            val snapshot = db.collection("promoCodes")
                .document(code)
                .get()
                .await()

            if (!snapshot.exists()) {
                return PromoResult.Error("유효하지 않은 이벤트 코드입니다")
            }

            val isActive = snapshot.getBoolean("isActive") ?: false
            if (!isActive) {
                return PromoResult.Error("만료된 코드입니다")
            }

            val expiresAt = snapshot.getDate("expiresAt")
            if (expiresAt != null && Date().after(expiresAt)) {
                return PromoResult.Error("만료된 코드입니다")
            }

            val maxUses = snapshot.getLong("maxUses")?.toInt() ?: 0
            val currentUses = snapshot.getLong("currentUses")?.toInt() ?: 0
            if (maxUses > 0 && currentUses >= maxUses) {
                return PromoResult.Error("코드 사용 한도를 초과했습니다")
            }

            val freeDays = snapshot.getLong("freeDays")?.toInt() ?: 0
            val discount = snapshot.getLong("discount")?.toInt() ?: 0
            val description = snapshot.getString("description") ?: "이벤트 코드가 적용되었습니다"

            // 사용 횟수 증가
            db.collection("promoCodes")
                .document(code)
                .update("currentUses", currentUses + 1)
                .await()

            // 코드 사용 기록 저장
            preferenceManager.saveUsedPromoCode(code)
            preferenceManager.savePromoCodeType("EVENT")

            return PromoResult.Success(
                type = PromoType.EVENT_CODE,
                message = description,
                freeDays = freeDays,
                discount = discount
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to validate event code: ${e.message}")
            return PromoResult.Error("코드 확인 중 오류가 발생했습니다")
        }
    }

    /**
     * 테스트 코드 적용
     */
    private fun applyTestCode(): PromoResult {
        preferenceManager.saveUsedPromoCode("TEST-FREE")
        preferenceManager.savePromoCodeType("TEST")

        return PromoResult.Success(
            type = PromoType.TEST_CODE,
            message = "테스트 코드가 적용되었습니다\n결제 없이 앱을 체험합니다",
            freeDays = 30
        )
    }

    /**
     * STANDFREE 코드 (출시 프로모션)
     */
    private fun applyStandFreeCode(): PromoResult {
        preferenceManager.saveUsedPromoCode("STANDFREE")
        preferenceManager.savePromoCodeType("LAUNCH_EVENT")

        return PromoResult.Success(
            type = PromoType.EVENT_CODE,
            message = "출시 기념 코드가 적용되었습니다!\n첫 달 무료로 시작하세요",
            freeDays = 30
        )
    }

    /**
     * 현재 적용된 프로모션 확인
     */
    fun getAppliedPromo(): Pair<String?, String?> {
        val code = preferenceManager.getAppliedPromoCode()
        val type = preferenceManager.getPromoCodeType()
        return Pair(code, type)
    }

    /**
     * 프로모션으로 인해 결제가 스킵되는지 확인
     */
    fun shouldSkipPayment(): Boolean {
        val type = preferenceManager.getPromoCodeType()
        return type in listOf("FRIEND_INVITE", "TEST", "LAUNCH_EVENT")
    }
}
