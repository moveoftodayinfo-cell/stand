package com.moveoftoday.walkorwait

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class BillingManager(
    private val context: Context,
    private val onPurchaseSuccess: (Purchase) -> Unit = {},
    private val onPurchaseFailure: (String) -> Unit = {},
    private val onConnectionReady: () -> Unit = {}
) {
    private val TAG = "BillingManager"

    private lateinit var billingClient: BillingClient
    private var isConnected = false
    private var isConnecting = false  // 연결 진행 중 상태 추적
    private var connectionRetryCount = 0
    private val maxRetries = 3
    private var pendingActivity: WeakReference<Activity>? = null  // 연결 대기 중인 Activity (WeakRef로 메모리 누수 방지)
    private var pendingPetChangeActivity: WeakReference<Activity>? = null  // 펫 변경 대기 중인 Activity

    // 상품 ID (Google Play Console에서 생성)
    companion object {
        // 단일 구독 상품: 월 4,900원 (한국/일본/미국 출시)
        const val SUBSCRIPTION_PRODUCT_ID = "standnew"
        // 펫 변경 일회성 상품: 1,000원
        const val PET_CHANGE_PRODUCT_ID = "pet_change"
    }

    fun isReady(): Boolean = isConnected

    // PurchasesUpdatedListener를 먼저 선언
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "⚠️ User canceled purchase")
            onPurchaseFailure("사용자가 결제를 취소했습니다")
        } else {
            Log.e(TAG, "❌ Purchase failed: ${billingResult.debugMessage}")
            onPurchaseFailure("결제에 실패했습니다: ${billingResult.debugMessage}")
        }
    }

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans() // 구독 지원 추가
                    .build()
            )
            .build()

        connectBillingClient()
    }

    /**
     * 에러 코드를 사람이 읽을 수 있는 메시지로 변환 (디버깅용 코드 포함)
     */
    private fun getErrorMessage(responseCode: Int): String {
        val message = when (responseCode) {
            BillingClient.BillingResponseCode.OK -> "성공"
            BillingClient.BillingResponseCode.USER_CANCELED -> "사용자가 취소했습니다"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Google Play 서비스에 연결할 수 없습니다. 인터넷 연결을 확인해주세요."
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Google Play 결제를 사용할 수 없습니다. Play Store에서 설치했는지 확인해주세요."
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "상품을 찾을 수 없습니다"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "개발자 오류 (상품 ID 확인 필요)"
            BillingClient.BillingResponseCode.ERROR -> "결제 시스템 오류"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "이미 구독 중입니다"
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "구독하지 않은 상품입니다"
            BillingClient.BillingResponseCode.NETWORK_ERROR -> "네트워크 오류. 인터넷 연결을 확인해주세요."
            else -> "알 수 없는 오류"
        }
        return "[$responseCode] $message"
    }

    private fun connectBillingClient() {
        if (isConnecting) {
            Log.d(TAG, "⏳ Already connecting, skipping...")
            return
        }

        isConnecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    connectionRetryCount = 0
                    Log.d(TAG, "✅ Billing client connected")

                    // 연결 성공 시 기존 구매 복원
                    queryPurchases()

                    // 연결 준비 콜백
                    onConnectionReady()

                    // 대기 중인 구독 요청이 있으면 실행
                    pendingActivity?.get()?.let { activity ->
                        Log.d(TAG, "📱 Processing pending subscription request")
                        pendingActivity = null
                        startSubscriptionInternal(activity)
                    }

                    // 대기 중인 펫 변경 요청이 있으면 실행
                    pendingPetChangeActivity?.get()?.let { activity ->
                        Log.d(TAG, "🐾 Processing pending pet change request")
                        pendingPetChangeActivity = null
                        startPetChangePurchaseInternal(activity)
                    }
                } else {
                    val errorMsg = getErrorMessage(billingResult.responseCode)
                    Log.e(TAG, "❌ Billing setup failed: code=${billingResult.responseCode}, msg=$errorMsg, debug=${billingResult.debugMessage}")
                    // 재시도
                    if (connectionRetryCount < maxRetries) {
                        connectionRetryCount++
                        Log.d(TAG, "🔄 Retrying connection... ($connectionRetryCount/$maxRetries)")
                        isConnecting = false
                        connectBillingClient()
                    } else {
                        pendingActivity = null
                        pendingPetChangeActivity = null
                        onPurchaseFailure("[0단계:연결실패] $errorMsg\n\n시도횟수: $connectionRetryCount/$maxRetries\n디버그: ${billingResult.debugMessage}\n\n※ Play Store 앱이 최신 버전인지, 로그인되어 있는지 확인하세요")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
                isConnecting = false
                Log.d(TAG, "⚠️ Billing service disconnected")
                // 재연결 시도
                if (connectionRetryCount < maxRetries) {
                    connectionRetryCount++
                    connectBillingClient()
                }
            }
        })
    }

    /**
     * 구독 시작 (결제 플로우)
     * - 단일 구독 상품: stand_subscription_monthly (4,900원/월)
     * - 달성률에 따른 할인은 프로모션 코드로 적용
     */
    fun startSubscription(activity: Activity) {
        // 이미 연결됨 - 바로 구독 시작
        if (isConnected) {
            Log.d(TAG, "✅ Already connected, starting subscription...")
            startSubscriptionInternal(activity)
            return
        }

        // 연결 진행 중 - 대기열에 추가
        if (isConnecting) {
            Log.d(TAG, "⏳ Connection in progress, queuing subscription request...")
            pendingActivity = WeakReference(activity)
            return
        }

        // 연결 안됨 - 연결 시도 후 구독 시작
        Log.d(TAG, "⏳ Billing client not connected, attempting to connect...")
        pendingActivity = WeakReference(activity)
        connectionRetryCount = 0
        connectBillingClient()
    }

    private fun startSubscriptionInternal(activity: Activity) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val productId = SUBSCRIPTION_PRODUCT_ID
                Log.d(TAG, "💰 Starting subscription: $productId")

                // 상품 정보 조회
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )

                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params)
                }

                // 상품 조회 결과 상세 로깅
                val queryCode = productDetailsResult.billingResult.responseCode
                val queryDebug = productDetailsResult.billingResult.debugMessage
                val productCount = productDetailsResult.productDetailsList?.size ?: 0

                Log.d(TAG, "📦 Query result code: $queryCode")
                Log.d(TAG, "📦 Query result message: $queryDebug")
                Log.d(TAG, "📦 Product list size: $productCount")

                if (queryCode != BillingClient.BillingResponseCode.OK) {
                    val errorMsg = getErrorMessage(queryCode)
                    Log.e(TAG, "❌ Query failed: $errorMsg")
                    withContext(Dispatchers.Main) {
                        onPurchaseFailure("[1단계:상품조회] $errorMsg\n\n상품ID: $productId\n디버그: $queryDebug")
                    }
                    return@launch
                }

                val productDetails = productDetailsResult.productDetailsList?.firstOrNull()

                if (productDetails == null) {
                    Log.e(TAG, "❌ Product not found - ID: $productId")
                    withContext(Dispatchers.Main) {
                        onPurchaseFailure("[2단계:상품없음] 상품을 찾을 수 없습니다\n\n상품ID: $productId\n조회된 상품 수: $productCount\n\n※ Play Console에서 '$productId' 구독 상품이 활성 상태인지 확인하세요")
                    }
                    return@launch
                }

                Log.d(TAG, "✅ Product found: ${productDetails.name}, ${productDetails.productId}")

                // 구독 상세 정보 로깅
                val offerDetailsList = productDetails.subscriptionOfferDetails
                val offerCount = offerDetailsList?.size ?: 0
                Log.d(TAG, "📋 Offer details count: $offerCount")

                val offerInfo = StringBuilder()
                offerDetailsList?.forEachIndexed { index, offer ->
                    Log.d(TAG, "📋 Offer[$index] basePlanId: ${offer.basePlanId}")
                    Log.d(TAG, "📋 Offer[$index] offerId: ${offer.offerId ?: "null"}")
                    Log.d(TAG, "📋 Offer[$index] offerToken: ${offer.offerToken}")
                    Log.d(TAG, "📋 Offer[$index] pricingPhases: ${offer.pricingPhases.pricingPhaseList.size}")
                    offerInfo.append("Offer[$index]: basePlan=${offer.basePlanId}, offerId=${offer.offerId ?: "없음"}\n")
                }

                // 구독 플로우 시작
                val offerToken = offerDetailsList?.firstOrNull()?.offerToken

                if (offerToken == null) {
                    Log.e(TAG, "❌ Offer token not found - subscriptionOfferDetails is empty or null")
                    withContext(Dispatchers.Main) {
                        onPurchaseFailure("[3단계:요금제없음] 기본 요금제를 찾을 수 없습니다\n\n상품ID: $productId\n상품명: ${productDetails.name}\n요금제 수: $offerCount\n\n※ Play Console에서 '$productId' 상품에 기본 요금제(Base Plan)가 활성화되어 있는지 확인하세요")
                    }
                    return@launch
                }

                Log.d(TAG, "🎫 Using offerToken: $offerToken")

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "🚀 Launching billing flow...")
                    val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                    val flowCode = billingResult.responseCode
                    val flowDebug = billingResult.debugMessage
                    Log.d(TAG, "🚀 Billing flow result: code=$flowCode, msg=$flowDebug")
                    if (flowCode != BillingClient.BillingResponseCode.OK) {
                        val errorMsg = getErrorMessage(flowCode)
                        Log.e(TAG, "❌ Failed to launch billing flow: $errorMsg, debug=$flowDebug")
                        onPurchaseFailure("[4단계:결제시작] $errorMsg\n\n상품: ${productDetails.name}\n${offerInfo}디버그: $flowDebug")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting subscription: ${e.message}")
                withContext(Dispatchers.Main) {
                    onPurchaseFailure("[예외발생] ${e.javaClass.simpleName}\n\n메시지: ${e.message}\n\n스택: ${e.stackTraceToString().take(500)}")
                }
            }
        }
    }

    /**
     * 구매 처리
     */
    private fun handlePurchase(purchase: Purchase) {
        Log.d(TAG, "📦 Handling purchase: ${purchase.orderId}")

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 펫 변경은 소비성 상품 (재구매 가능)
            if (purchase.products.contains(PET_CHANGE_PRODUCT_ID)) {
                consumePurchase(purchase)
            } else if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                onPurchaseSuccess(purchase)
            }
        } else {
            Log.d(TAG, "⚠️ Purchase not in PURCHASED state: ${purchase.purchaseState}")
        }
    }

    /**
     * 구매 확인 (Acknowledge)
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "✅ Purchase acknowledged")

                // Analytics: 구독 결제 추적
                val productId = purchase.products.firstOrNull() ?: "stand_monthly"
                AnalyticsManager.trackPurchaseCompleted(productId, 4700.0)
                AnalyticsManager.trackSubscriptionStart("google_play")

                withContext(Dispatchers.Main) {
                    onPurchaseSuccess(purchase)
                }
            } else {
                Log.e(TAG, "❌ Failed to acknowledge purchase: ${result.debugMessage}")
            }
        }
    }

    /**
     * 기존 구매 복원 (구독 + 소비성 상품)
     */
    private fun queryPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. 구독 상품 조회
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val subsResult = billingClient.queryPurchasesAsync(subsParams)
            val subsPurchases = subsResult.purchasesList

            if (subsPurchases.isNotEmpty()) {
                Log.d(TAG, "📦 Found ${subsPurchases.size} subscription(s)")
                for (purchase in subsPurchases) {
                    handlePurchase(purchase)
                }
            }

            // 2. 소비성 상품 조회 (unconsumed 펫 변경 감지)
            val inappParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            val inappResult = billingClient.queryPurchasesAsync(inappParams)
            val inappPurchases = inappResult.purchasesList

            if (inappPurchases.isNotEmpty()) {
                Log.d(TAG, "📦 Found ${inappPurchases.size} unconsumed INAPP purchase(s)")
                for (purchase in inappPurchases) {
                    // Unconsumed purchase 발견 - 재시도
                    Log.d(TAG, "🔄 Retrying unconsumed purchase: ${purchase.orderId}")
                    handlePurchase(purchase)
                }
            }

            if (subsPurchases.isEmpty() && inappPurchases.isEmpty()) {
                Log.d(TAG, "📦 No existing purchases found")
            }
        }
    }

    /**
     * 활성 구독 확인
     */
    fun checkActiveSubscription(callback: (Boolean, Purchase?) -> Unit) {
        if (!isConnected) {
            callback(false, null)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val purchasesResult = billingClient.queryPurchasesAsync(params)
            val purchases = purchasesResult.purchasesList

            val activePurchase = purchases.firstOrNull { purchase ->
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(SUBSCRIPTION_PRODUCT_ID)
            }

            withContext(Dispatchers.Main) {
                callback(activePurchase != null, activePurchase)
            }
        }
    }

    /**
     * 펫 변경 구매 (일회성 1,000원)
     */
    fun startPetChangePurchase(activity: Activity) {
        // 이미 연결됨 - 바로 구매 시작
        if (isConnected) {
            Log.d(TAG, "✅ Already connected, starting pet change purchase...")
            startPetChangePurchaseInternal(activity)
            return
        }

        // 연결 진행 중 - 대기열에 추가
        if (isConnecting) {
            Log.d(TAG, "⏳ Connection in progress, queuing pet change request...")
            pendingPetChangeActivity = WeakReference(activity)
            return
        }

        // 연결 안됨 - 연결 시도 후 구매 시작
        Log.d(TAG, "⏳ Billing client not connected, attempting to connect for pet change...")
        pendingPetChangeActivity = WeakReference(activity)
        connectionRetryCount = 0
        connectBillingClient()
    }

    private fun startPetChangePurchaseInternal(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "🐾 Starting pet change purchase")

                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PET_CHANGE_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )

                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                val productDetailsResult = billingClient.queryProductDetails(params)

                if (productDetailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    val errorMsg = getErrorMessage(productDetailsResult.billingResult.responseCode)
                    withContext(Dispatchers.Main) {
                        onPurchaseFailure("[펫변경] 상품 조회 실패: $errorMsg")
                    }
                    return@launch
                }

                val productDetails = productDetailsResult.productDetailsList?.firstOrNull()
                if (productDetails == null) {
                    withContext(Dispatchers.Main) {
                        onPurchaseFailure("[펫변경] 상품을 찾을 수 없습니다\n\nPlay Console에서 '$PET_CHANGE_PRODUCT_ID' 상품이 활성 상태인지 확인하세요")
                    }
                    return@launch
                }

                Log.d(TAG, "✅ Pet change product found: ${productDetails.name}")

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                withContext(Dispatchers.Main) {
                    val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        val errorMsg = getErrorMessage(billingResult.responseCode)
                        onPurchaseFailure("[펫변경] 결제 시작 실패: $errorMsg")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting pet change purchase: ${e.message}")
                withContext(Dispatchers.Main) {
                    onPurchaseFailure("[펫변경] 오류: ${e.message}")
                }
            }
        }
    }

    /**
     * 일회성 구매 소비 (재구매 가능하게)
     */
    private fun consumePurchase(purchase: Purchase, retryCount: Int = 0) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            // Analytics 추적 (consume 전에)
            val productId = purchase.products.firstOrNull() ?: "pet_change"

            // 첫 시도일 때만 purchase completed 추적
            if (retryCount == 0) {
                AnalyticsManager.trackPurchaseCompleted(productId, 1000.0)

                // ✅ 결제는 완료되었으므로 일단 서비스 제공
                withContext(Dispatchers.Main) {
                    onPurchaseSuccess(purchase)
                }
            }

            // consume 시도 (재구매 가능하게)
            val result = billingClient.consumePurchase(consumeParams)
            val responseCode = result.billingResult.responseCode

            if (responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "✅ Purchase consumed successfully (can buy again)")
                if (retryCount > 0) {
                    Log.d(TAG, "🔄 Consume succeeded after $retryCount retry(ies)")
                }
            } else {
                val errorMsg = result.billingResult.debugMessage
                Log.e(TAG, "⚠️ Consume failed [attempt ${retryCount + 1}]: $responseCode - $errorMsg")

                // Firebase Analytics: consume 실패 추적
                AnalyticsManager.trackError(
                    "billing_consume_failed",
                    "code=$responseCode, retry=$retryCount, product=$productId"
                )

                // 에러 타입별 처리
                when (responseCode) {
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                        // 일시적 오류 - 재시도 (최대 3회)
                        if (retryCount < 3) {
                            val delayMs = (retryCount + 1) * 2000L // 2초, 4초, 6초
                            Log.d(TAG, "🔄 Retrying consume in ${delayMs}ms...")
                            kotlinx.coroutines.delay(delayMs)
                            consumePurchase(purchase, retryCount + 1)
                        } else {
                            Log.e(TAG, "❌ Consume retry limit reached (3 attempts)")
                            Log.d(TAG, "⚠️ Purchase will be retried on next app launch (queryPurchases)")
                        }
                    }
                    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                        // 이미 소비되었거나 환불됨 - 재시도 불필요
                        Log.w(TAG, "⚠️ Purchase not owned - already consumed or refunded")
                    }
                    else -> {
                        // 기타 오류 - 다음 앱 실행 시 queryPurchases에서 재시도
                        Log.e(TAG, "⚠️ Consume failed with code $responseCode")
                        Log.d(TAG, "⚠️ Will retry on next app launch")
                    }
                }

                if (retryCount == 0) {
                    Log.d(TAG, "✅ Pet change already applied (user paid)")
                }
            }
        }
    }

    /**
     * 구독 관리 화면 열기 (Google Play Store)
     */
    fun openSubscriptionManagement(activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions")
            }
            activity.startActivity(intent)
            Log.d(TAG, "📱 Opening subscription management")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to open subscription management: ${e.message}")
        }
    }

    /**
     * 리소스 정리
     */
    fun destroy() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
            isConnected = false
        }
    }
}
