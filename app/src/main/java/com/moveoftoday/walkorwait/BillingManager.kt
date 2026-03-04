package com.moveoftoday.walkorwait

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    // Lifecycle-managed CoroutineScope (메모리 누수 방지)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var billingClient: BillingClient
    private var isConnected = false
    private var isConnecting = false  // 연결 진행 중 상태 추적
    private var connectionRetryCount = 0
    private val maxRetries = 3
    private var pendingActivity: WeakReference<Activity>? = null  // 연결 대기 중인 Activity (WeakRef로 메모리 누수 방지)
    private var pendingPetChangeActivity: WeakReference<Activity>? = null  // 펫 변경 대기 중인 Activity

    // Race condition 방지: 현재 처리 중인 구매 토큰 추적
    private val processingPurchases = mutableSetOf<String>()

    // 상품 ID (Google Play Console에서 생성)
    companion object {
        // 구독 상품 ID (월간/연간 모두 동일, base plan으로 구분)
        const val SUBSCRIPTION_PRODUCT_ID = "standnew"
        // Base Plan IDs (Play Console에서 설정한 값)
        const val BASE_PLAN_MONTHLY = "plan-monthly"
        const val BASE_PLAN_YEARLY = "plan-yearly"
        // 펫 변경 일회성 상품: 1,000원
        const val PET_CHANGE_PRODUCT_ID = "pet_change"
    }

    fun isReady(): Boolean = isConnected

    // 펫 변경 상품 가격 캐시
    private var petChangePriceCache: String? = null

    /**
     * 펫 변경 상품 가격 조회 (Google Play에서 실제 가격)
     * 국가별 통화로 자동 변환됨
     */
    suspend fun getPetChangePrice(): String? {
        // 캐시된 가격 반환
        petChangePriceCache?.let { return it }

        if (!isConnected) return null

        return try {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PET_CHANGE_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = billingClient.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val price = result.productDetailsList?.firstOrNull()
                    ?.oneTimePurchaseOfferDetails?.formattedPrice
                petChangePriceCache = price
                price
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get pet change price: ${e.message}")
            null
        }
    }

    // 구독 가격 캐시
    private var monthlyPriceCache: String? = null
    private var yearlyPriceCache: String? = null
    private var dailyPriceCache: String? = null  // 연간을 일 단위로 계산

    /**
     * 구독 가격 정보 데이터 클래스
     */
    data class SubscriptionPrices(
        val monthlyPrice: String?,      // "₩3,900", "$2.99" 등
        val yearlyPrice: String?,       // "₩26,900", "$19.99" 등
        val dailyPrice: String?         // "₩74", "$0.05" 등 (연간/365)
    )

    /**
     * 구독 가격 조회 (Google Play에서 실제 가격)
     * 월간, 연간, 일간(연간/365) 가격 반환
     */
    suspend fun getSubscriptionPrices(): SubscriptionPrices {
        // 캐시된 가격 반환
        if (monthlyPriceCache != null && yearlyPriceCache != null) {
            return SubscriptionPrices(monthlyPriceCache, yearlyPriceCache, dailyPriceCache)
        }

        if (!isConnected) {
            return SubscriptionPrices(null, null, null)
        }

        return try {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(SUBSCRIPTION_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = billingClient.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = result.productDetailsList?.firstOrNull()
                val offerDetails = productDetails?.subscriptionOfferDetails

                // Monthly 가격 찾기
                val monthlyOffer = offerDetails?.find { it.basePlanId == BASE_PLAN_MONTHLY }
                val monthlyPhase = monthlyOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                monthlyPriceCache = monthlyPhase?.formattedPrice

                // Yearly 가격 찾기
                val yearlyOffer = offerDetails?.find { it.basePlanId == BASE_PLAN_YEARLY }
                val yearlyPhase = yearlyOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                yearlyPriceCache = yearlyPhase?.formattedPrice

                // Daily 가격 계산 (연간가격 / 365)
                val yearlyMicros = yearlyPhase?.priceAmountMicros ?: 0
                val currencyCode = yearlyPhase?.priceCurrencyCode ?: "USD"
                if (yearlyMicros > 0) {
                    val dailyMicros = yearlyMicros / 365
                    dailyPriceCache = formatMicrosToPrice(dailyMicros, currencyCode)
                }

                Log.d(TAG, "💰 Subscription prices: monthly=$monthlyPriceCache, yearly=$yearlyPriceCache, daily=$dailyPriceCache")

                SubscriptionPrices(monthlyPriceCache, yearlyPriceCache, dailyPriceCache)
            } else {
                SubscriptionPrices(null, null, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get subscription prices: ${e.message}")
            SubscriptionPrices(null, null, null)
        }
    }

    /**
     * 마이크로 단위 가격을 포맷된 문자열로 변환
     */
    private fun formatMicrosToPrice(micros: Long, currencyCode: String): String {
        val amount = micros / 1_000_000.0
        return when (currencyCode) {
            "KRW" -> "₩${amount.toInt()}"
            "JPY" -> "¥${amount.toInt()}"
            "USD" -> "$${String.format("%.2f", amount)}"
            "EUR" -> "€${String.format("%.2f", amount)}"
            "GBP" -> "£${String.format("%.2f", amount)}"
            "CNY" -> "¥${String.format("%.2f", amount)}"
            "INR" -> "₹${amount.toInt()}"
            "BRL" -> "R$${String.format("%.2f", amount)}"
            else -> "$${String.format("%.2f", amount)}"
        }
    }

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
                        Log.d(TAG, "📱 Processing pending subscription request (${pendingSubscriptionType})")
                        pendingActivity = null
                        startSubscriptionInternal(activity, pendingSubscriptionType)
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

    // 대기 중인 구독 타입 저장
    private var pendingSubscriptionType: SubscriptionType = SubscriptionType.MONTHLY

    // 마지막 결제의 실제 가격 정보 (Google Play에서 받은 값)
    var lastPurchaseFormattedPrice: String? = null
        private set
    var lastPurchaseCurrencyCode: String? = null
        private set
    var lastPurchasePriceMicros: Long? = null
        private set

    enum class SubscriptionType {
        MONTHLY,  // 월간: 3,900원/월
        YEARLY    // 연간: 39,000원/년 (2개월 무료)
    }

    /**
     * 구독 시작 (결제 플로우)
     * - 월간 구독: 3,900원/월 (7일 무료 체험)
     * - 연간 구독: 39,000원/년 (7일 무료 체험, 2개월 무료)
     */
    fun startSubscription(activity: Activity, type: SubscriptionType = SubscriptionType.MONTHLY) {
        pendingSubscriptionType = type

        // 이미 연결됨 - 바로 구독 시작
        if (isConnected) {
            Log.d(TAG, "✅ Already connected, starting subscription...")
            startSubscriptionInternal(activity, type)
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

    private fun startSubscriptionInternal(activity: Activity, type: SubscriptionType = SubscriptionType.MONTHLY) {

        scope.launch {
            try {
                // 월간/연간 모두 동일한 상품 ID 사용, base plan으로 구분
                val productId = SUBSCRIPTION_PRODUCT_ID
                val targetBasePlan = when (type) {
                    SubscriptionType.MONTHLY -> BASE_PLAN_MONTHLY
                    SubscriptionType.YEARLY -> BASE_PLAN_YEARLY
                }
                Log.d(TAG, "💰 Starting subscription: $productId (basePlan: $targetBasePlan)")

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

                // 구독 플로우 시작 - 대상 base plan에 해당하는 offer 찾기
                val targetOffer = offerDetailsList?.find { it.basePlanId == targetBasePlan }
                val offerToken = targetOffer?.offerToken

                if (offerToken == null) {
                    Log.e(TAG, "❌ Offer token not found for basePlan: $targetBasePlan")
                    Log.e(TAG, "❌ Available base plans: ${offerDetailsList?.map { it.basePlanId }}")
                    withContext(Dispatchers.Main) {
                        onPurchaseFailure("[3단계:요금제없음] '$targetBasePlan' 요금제를 찾을 수 없습니다\n\n상품ID: $productId\n상품명: ${productDetails.name}\n요금제 수: $offerCount\n가능한 요금제: ${offerDetailsList?.map { it.basePlanId }}\n\n※ Play Console에서 '$targetBasePlan' 기본 요금제(Base Plan)가 활성화되어 있는지 확인하세요")
                    }
                    return@launch
                }

                Log.d(TAG, "🎫 Using basePlan: $targetBasePlan, offerToken: $offerToken")

                // 실제 가격 정보 캐시 (Firestore 저장용)
                val pricingPhase = targetOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                lastPurchaseFormattedPrice = pricingPhase?.formattedPrice
                lastPurchaseCurrencyCode = pricingPhase?.priceCurrencyCode
                lastPurchasePriceMicros = pricingPhase?.priceAmountMicros
                Log.d(TAG, "💰 Actual price: $lastPurchaseFormattedPrice ($lastPurchaseCurrencyCode, ${lastPurchasePriceMicros}μ)")

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
     * 구매 처리 (race condition 방지)
     */
    private fun handlePurchase(purchase: Purchase) {
        val purchaseToken = purchase.purchaseToken

        // 이미 처리 중인 구매인지 확인 (synchronized로 thread-safe)
        synchronized(processingPurchases) {
            if (processingPurchases.contains(purchaseToken)) {
                Log.d(TAG, "⏳ Purchase already being processed, skipping: ${purchase.orderId}")
                return
            }
            // 처리 시작 표시
            processingPurchases.add(purchaseToken)
        }

        Log.d(TAG, "📦 Handling purchase: ${purchase.orderId}")

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 펫 변경은 소비성 상품 (재구매 가능)
            if (purchase.products.contains(PET_CHANGE_PRODUCT_ID)) {
                consumePurchase(purchase)
            } else if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                onPurchaseSuccess(purchase)
                // 처리 완료 - 토큰 제거
                synchronized(processingPurchases) {
                    processingPurchases.remove(purchaseToken)
                }
            }
        } else {
            Log.d(TAG, "⚠️ Purchase not in PURCHASED state: ${purchase.purchaseState}")
            // 처리 완료 - 토큰 제거
            synchronized(processingPurchases) {
                processingPurchases.remove(purchaseToken)
            }
        }
    }

    /**
     * 구매 확인 (Acknowledge)
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val purchaseToken = purchase.purchaseToken
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        scope.launch {
            try {
                val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "✅ Purchase acknowledged")

                    // Analytics: 구독 결제 추적
                    val productId = purchase.products.firstOrNull() ?: SUBSCRIPTION_PRODUCT_ID
                    val isYearly = pendingSubscriptionType == SubscriptionType.YEARLY
                    val price = if (isYearly) 39000.0 else 3900.0
                    AnalyticsManager.trackPurchaseCompleted(productId, price)
                    AnalyticsManager.trackSubscriptionStart(if (isYearly) "google_play_yearly" else "google_play_monthly")

                    // 친구 초대 횟수 초기화 (월간: 1명, 연간: 12명)
                    val preferenceManager = PreferenceManager(context)
                    preferenceManager.initInvitesForSubscription(isYearly)
                    Log.d(TAG, "✅ Invites initialized: ${if (isYearly) 12 else 1} (${pendingSubscriptionType})")

                    // 첫 실제 결제일 저장 (친구 초대 코드 3일 후 활성화용)
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    preferenceManager.saveFirstPaidDate(today)
                    Log.d(TAG, "✅ First paid date saved: $today")

                    withContext(Dispatchers.Main) {
                        onPurchaseSuccess(purchase)
                    }
                } else {
                    Log.e(TAG, "❌ Failed to acknowledge purchase: ${result.debugMessage}")
                }
            } finally {
                // 처리 완료 - 토큰 제거
                synchronized(processingPurchases) {
                    processingPurchases.remove(purchaseToken)
                }
            }
        }
    }

    /**
     * 기존 구매 복원 (구독 + 소비성 상품)
     */
    private fun queryPurchases() {
        scope.launch {
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

        scope.launch {
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
        scope.launch {
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
        val purchaseToken = purchase.purchaseToken
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        scope.launch {
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
                // 처리 완료 - 토큰 제거
                synchronized(processingPurchases) {
                    processingPurchases.remove(purchaseToken)
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
                            return@launch // 재귀 호출에서 토큰 제거 처리
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

                // 재시도 없이 종료되는 경우 - 토큰 제거 (다음 앱 실행 시 재시도 가능)
                synchronized(processingPurchases) {
                    processingPurchases.remove(purchaseToken)
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
     * 리소스 정리 (Activity/Fragment onDestroy에서 호출)
     */
    fun destroy() {
        // CoroutineScope 취소 (메모리 누수 방지)
        scope.cancel()

        if (::billingClient.isInitialized) {
            billingClient.endConnection()
            isConnected = false
        }
        Log.d(TAG, "🧹 BillingManager destroyed")
    }
}
