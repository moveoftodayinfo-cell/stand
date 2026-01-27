package com.moveoftoday.walkorwait

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltAndroidApp
class WalkorWaitApp : Application() {
    private val TAG = "WalkorWaitApp"

    // Application 레벨 코루틴 스코프
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Firebase Auth 인스턴스 (싱글톤)
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Repository (싱글톤)
    lateinit var userDataRepository: UserDataRepository
        private set

    // BillingManager (구독 상태 확인용)
    private var billingManager: BillingManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Application started")

        // Analytics 초기화
        AnalyticsManager.initialize(this)

        // Repository 초기화 (먼저 생성, 동기화는 나중에)
        userDataRepository = UserDataRepository(
            context = this,
            auth = auth,
            autoSync = false  // 자동 동기화 비활성화
        )

        // Firebase 익명 로그인 후 Repository 동기화
        initializeFirebaseAuthAndSync()

        // 구독 상태 확인
        verifySubscriptionStatus()
    }

    private fun initializeFirebaseAuthAndSync() {
        applicationScope.launch {
            try {
                // 1. 먼저 Firebase 인증 완료
                if (auth.currentUser == null) {
                    Log.d(TAG, "📱 Signing in anonymously...")
                    auth.signInAnonymously().await()
                    Log.d(TAG, "✅ Firebase Auth initialized: ${auth.currentUser?.uid}")
                } else {
                    Log.d(TAG, "✅ Already signed in: ${auth.currentUser?.uid}")
                }

                // 2. 인증 완료 후 Repository 동기화 시작
                Log.d(TAG, "🔄 Starting repository sync after auth...")
                userDataRepository.startSync()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Firebase Auth failed: ${e.message}")
                // 인증 실패해도 syncCompleted를 true로 설정하여 앱이 멈추지 않게
                userDataRepository.markSyncCompleted()
            }
        }
    }

    /**
     * Google Play 구독 상태 확인
     * 앱 시작 시 구독이 취소되었는지 확인
     */
    private fun verifySubscriptionStatus() {
        val preferenceManager = PreferenceManager(this)

        // 프로모션 무료 기간인 경우 Google Play 확인 스킵
        if (preferenceManager.isInPromoFreePeriod()) {
            Log.d(TAG, "🎁 In promo free period - skipping Google Play check")
            return
        }

        // 결제된 적이 있는 경우에만 확인
        if (!preferenceManager.isPaidDeposit()) {
            Log.d(TAG, "📱 No subscription to verify")
            return
        }

        billingManager = BillingManager(
            context = this,
            onPurchaseSuccess = { purchase ->
                Log.d(TAG, "✅ Subscription active: ${purchase.orderId}")
            },
            onPurchaseFailure = { error ->
                Log.d(TAG, "⚠️ Subscription check: $error")
            }
        )

        billingManager?.checkActiveSubscription { isActive, purchase ->
            if (isActive) {
                Log.d(TAG, "✅ Subscription verified: ${purchase?.orderId}")
            } else {
                Log.d(TAG, "⚠️ No active subscription found")

                // 구독이 취소된 경우에도 현재 기간은 유지
                // (Google Play에서 기간 만료 전까지는 사용 가능)
                // 여기서는 로그만 남기고, 실제 비활성화는 기간 만료 시 처리
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        billingManager?.destroy()
    }
}
