package com.moveoftoday.walkorwait

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.MockupColors

/**
 * TutorialScreen internationalization helper
 */
private object TutorialStrings {
    private fun getLang(): String = java.util.Locale.getDefault().language

    // Subscription Step
    fun subscriptionPlan(): String = when (getLang()) {
        "ko" -> "구독 플랜"
        "ja" -> "サブスクリプションプラン"
        "zh" -> "订阅计划"
        "es" -> "Plan de suscripcion"
        else -> "Subscription Plan"
    }

    fun autoPaymentAfterTrial(): String = when (getLang()) {
        "ko" -> "무료 체험 후 자동 결제"
        "ja" -> "無料体験後に自動課金"
        "zh" -> "免费试用后自动付款"
        "es" -> "Pago automatico despues de la prueba"
        else -> "Auto payment after free trial"
    }

    fun monthlySubscription(): String = when (getLang()) {
        "ko" -> "월간 구독"
        "ja" -> "月額プラン"
        "zh" -> "月度订阅"
        "es" -> "Suscripcion mensual"
        else -> "Monthly"
    }

    fun yearlySubscription(): String = when (getLang()) {
        "ko" -> "연간 구독"
        "ja" -> "年間プラン"
        "zh" -> "年度订阅"
        "es" -> "Suscripcion anual"
        else -> "Yearly"
    }

    fun twoMonthsFree(): String = when (getLang()) {
        "ko" -> "2개월 무료"
        "ja" -> "2ヶ月無料"
        "zh" -> "免费2个月"
        "es" -> "2 meses gratis"
        else -> "2 months free"
    }

    fun streakProtectionTicket(): String = when (getLang()) {
        "ko" -> "streak 방어 티켓"
        "ja" -> "ストリーク保護チケット"
        "zh" -> "连续记录保护票"
        "es" -> "Ticket de proteccion de racha"
        else -> "Streak Protection Ticket"
    }

    fun awardedByGoalRate(): String = when (getLang()) {
        "ko" -> "매달 목표 달성률에 따라 지급"
        "ja" -> "毎月の目標達成率に応じて付与"
        "zh" -> "根据每月目标完成率发放"
        "es" -> "Otorgado segun la tasa de logro mensual"
        else -> "Awarded based on monthly goal rate"
    }

    fun subscriptionNote(): String = when (getLang()) {
        "ko" -> "* 무료 체험 후 자동 결제\n* 언제든 구독 취소 가능"
        "ja" -> "* 無料体験後に自動課金\n* いつでも解約可能"
        "zh" -> "* 免费试用后自动付款\n* 随时可取消订阅"
        "es" -> "* Pago automatico despues de la prueba\n* Cancela en cualquier momento"
        else -> "* Auto payment after free trial\n* Cancel anytime"
    }

    fun getStarted(): String = when (getLang()) {
        "ko" -> "시작하기"
        "ja" -> "始める"
        "zh" -> "开始"
        "es" -> "Comenzar"
        else -> "Get Started"
    }

    fun monthlyPrice(): String = when (getLang()) {
        "ko" -> "월 3,900원"
        "ja" -> "月額 ¥390"
        "zh" -> "每月 ¥25"
        "es" -> "$3.99/mes"
        else -> "$3.99/mo"
    }

    fun yearlyPrice(): String = when (getLang()) {
        "ko" -> "연 39,000원"
        "ja" -> "年額 ¥3,900"
        "zh" -> "每年 ¥250"
        "es" -> "$39.99/ano"
        else -> "$39.99/yr"
    }

    // Step unit
    fun stepsUnit(): String = when (getLang()) {
        "ko" -> "보"
        "ja" -> "歩"
        "zh" -> "步"
        "es" -> "pasos"
        else -> "steps"
    }

    fun formatStepsTarget(steps: Int): String {
        val unit = stepsUnit()
        return when (getLang()) {
            "ko", "ja", "zh" -> "$steps$unit"
            else -> "$steps $unit"
        }
    }

    // Ticket count unit
    fun ticketCount(count: Int): String = when (getLang()) {
        "ko" -> "${count}개"
        "ja" -> "${count}枚"
        "zh" -> "${count}张"
        "es" -> "$count"
        else -> "$count"
    }
}

/**
 * 접근성 권한 동의 다국어 문자열 (Google Play 정책 준수)
 * - Google 권장 형식: "[앱]은 [기능]을 위해 [데이터]를 수집합니다"
 * - AccessibilityService API 명칭 명시
 * - TYPE_WINDOW_STATE_CHANGED 이벤트 명시
 * - 4개 체크박스로 명시적 동의
 * - 동의 철회 방법 안내
 */
private object TutorialAccessibilityStrings {
    private fun getLang(): String = java.util.Locale.getDefault().language

    // 제목
    fun title(): String = when (getLang()) {
        "ko" -> "AccessibilityService API 사용에 대한 명시적 공개"
        "ja" -> "AccessibilityService API使用に関する明示的な開示"
        "zh" -> "AccessibilityService API使用的明确披露"
        "es" -> "Divulgacion Explicita del Uso de AccessibilityService API"
        else -> "Explicit Disclosure of AccessibilityService API Usage"
    }

    // Google 권장 형식 공개 문구
    fun prominentDisclosure(): String = when (getLang()) {
        "ko" -> "rebon은 앱 차단 기능을 사용 설정하기 위해 " +
                "현재 실행 중인 앱의 패키지명(TYPE_WINDOW_STATE_CHANGED 이벤트)을 수집합니다. " +
                "이 데이터는 기기 내에서만 처리되며 외부 서버로 전송되지 않습니다."
        "ja" -> "rebonは、アプリブロック機能を有効にするために、" +
                "現在実行中のアプリのパッケージ名(TYPE_WINDOW_STATE_CHANGEDイベント)を収集します。" +
                "このデータはデバイス内でのみ処理され、外部サーバーには送信されません。"
        "zh" -> "rebon为了启用应用阻止功能，会收集当前运行应用的包名" +
                "(TYPE_WINDOW_STATE_CHANGED事件)。" +
                "此数据仅在设备内处理，不会传输到外部服务器。"
        "es" -> "rebon recopila el nombre del paquete de la aplicacion en ejecucion " +
                "(evento TYPE_WINDOW_STATE_CHANGED) para habilitar la funcion de bloqueo de apps. " +
                "Estos datos se procesan solo en el dispositivo y no se envian a servidores externos."
        else -> "rebon collects the package name of the currently running app " +
                "(TYPE_WINDOW_STATE_CHANGED event) to enable the app blocking feature. " +
                "This data is processed only on the device and is not transmitted to external servers."
    }

    // 섹션 1: 수집하는 데이터
    fun sectionDataCollected(): String = when (getLang()) {
        "ko" -> "1. 수집하는 데이터"
        "ja" -> "1. 収集するデータ"
        "zh" -> "1. 收集的数据"
        "es" -> "1. Datos recopilados"
        else -> "1. Data collected"
    }

    fun dataCollectedContent(): String = when (getLang()) {
        "ko" -> "- 현재 화면에 표시된 앱의 패키지명\n" +
                "- TYPE_WINDOW_STATE_CHANGED 이벤트를 통해 수집\n" +
                "- 앱 차단 여부 판단에만 사용"
        "ja" -> "- 現在表示中のアプリのパッケージ名\n" +
                "- TYPE_WINDOW_STATE_CHANGEDイベントで収集\n" +
                "- アプリブロック判定にのみ使用"
        "zh" -> "- 当前显示应用的包名\n" +
                "- 通过TYPE_WINDOW_STATE_CHANGED事件收集\n" +
                "- 仅用于判断应用阻止"
        "es" -> "- Nombre del paquete de la app actual\n" +
                "- Recopilado via evento TYPE_WINDOW_STATE_CHANGED\n" +
                "- Usado solo para determinar bloqueo"
        else -> "- Package name of the currently displayed app\n" +
                "- Collected via TYPE_WINDOW_STATE_CHANGED event\n" +
                "- Used only to determine app blocking"
    }

    // 섹션 2: 데이터 사용 방법
    fun sectionDataUsage(): String = when (getLang()) {
        "ko" -> "2. 데이터 사용 방법"
        "ja" -> "2. データの使用方法"
        "zh" -> "2. 数据使用方式"
        "es" -> "2. Como se usan los datos"
        else -> "2. How data is used"
    }

    fun dataUsageContent(): String = when (getLang()) {
        "ko" -> "- 사용자가 차단 설정한 앱인지 확인\n" +
                "- 차단 앱 실행 시 목표 미달성이면 홈으로 이동\n" +
                "- 모든 처리는 기기 내에서만 수행\n" +
                "- 외부 서버 전송 없음"
        "ja" -> "- ユーザーがブロック設定したアプリか確認\n" +
                "- ブロックアプリ起動時、目標未達成ならホームへ\n" +
                "- 全処理はデバイス内のみ\n" +
                "- 外部サーバー送信なし"
        "zh" -> "- 确认是否为用户设置阻止的应用\n" +
                "- 打开阻止应用时若目标未达成则返回主屏\n" +
                "- 所有处理仅在设备内进行\n" +
                "- 不传输到外部服务器"
        "es" -> "- Verificar si es una app bloqueada\n" +
                "- Si se abre app bloqueada sin meta, ir a inicio\n" +
                "- Todo el procesamiento es local\n" +
                "- Sin envio a servidores externos"
        else -> "- Check if it is an app blocked by user\n" +
                "- If blocked app opens without goal met, go to home\n" +
                "- All processing is on-device only\n" +
                "- No transmission to external servers"
    }

    // 섹션 3: 수집하지 않는 데이터
    fun sectionDataNotCollected(): String = when (getLang()) {
        "ko" -> "3. 수집하지 않는 데이터"
        "ja" -> "3. 収集しないデータ"
        "zh" -> "3. 不收集的数据"
        "es" -> "3. Datos NO recopilados"
        else -> "3. Data NOT collected"
    }

    fun dataNotCollectedContent(): String = when (getLang()) {
        "ko" -> "- 화면 내용, 입력 텍스트, 비밀번호\n" +
                "- 앱 사용 기록, 브라우징 기록\n" +
                "- 개인정보, 위치정보\n" +
                "- 제3자 공유 없음"
        "ja" -> "- 画面内容、入力テキスト、パスワード\n" +
                "- アプリ使用履歴、ブラウジング履歴\n" +
                "- 個人情報、位置情報\n" +
                "- 第三者共有なし"
        "zh" -> "- 屏幕内容、输入文字、密码\n" +
                "- 应用使用记录、浏览记录\n" +
                "- 个人信息、位置信息\n" +
                "- 不与第三方共享"
        "es" -> "- Contenido de pantalla, texto, contrasenas\n" +
                "- Historial de uso, historial de navegacion\n" +
                "- Informacion personal, ubicacion\n" +
                "- Sin compartir con terceros"
        else -> "- Screen content, input text, passwords\n" +
                "- App usage history, browsing history\n" +
                "- Personal information, location\n" +
                "- No third-party sharing"
    }

    // 섹션 4: 동의 철회 방법
    fun sectionWithdrawal(): String = when (getLang()) {
        "ko" -> "4. 동의 철회 방법"
        "ja" -> "4. 同意撤回方法"
        "zh" -> "4. 撤回同意的方法"
        "es" -> "4. Como revocar el consentimiento"
        else -> "4. How to withdraw consent"
    }

    fun withdrawalContent(): String = when (getLang()) {
        "ko" -> "설정 > 접근성 > 설치된 서비스 > rebon > 사용 안 함\n\n" +
                "언제든지 위 경로에서 접근성 서비스를 비활성화할 수 있습니다."
        "ja" -> "設定 > ユーザー補助 > インストール済みサービス > rebon > オフ\n\n" +
                "いつでも上記の経路でサービスを無効化できます。"
        "zh" -> "设置 > 无障碍 > 已安装的服务 > rebon > 关闭\n\n" +
                "您可以随时通过上述路径禁用无障碍服务。"
        "es" -> "Ajustes > Accesibilidad > Servicios instalados > rebon > Desactivar\n\n" +
                "Puede desactivar el servicio en cualquier momento."
        else -> "Settings > Accessibility > Installed services > rebon > Turn off\n\n" +
                "You can disable the service at any time through the above path."
    }

    // 체크박스 1: 데이터 수집 이해
    fun checkbox1DataCollection(): String = when (getLang()) {
        "ko" -> "위 '수집하는 데이터' 내용을 읽고 이해했습니다."
        "ja" -> "上記の'収集するデータ'の内容を読んで理解しました。"
        "zh" -> "我已阅读并理解上述'收集的数据'内容。"
        "es" -> "He leido y comprendido 'Datos recopilados'."
        else -> "I have read and understood 'Data collected' above."
    }

    // 체크박스 2: 데이터 사용 이해
    fun checkbox2DataUsage(): String = when (getLang()) {
        "ko" -> "위 '데이터 사용 방법' 내용을 읽고 이해했습니다."
        "ja" -> "上記の'データの使用方法'の内容を読んで理解しました。"
        "zh" -> "我已阅读并理解上述'数据使用方式'内容。"
        "es" -> "He leido y comprendido 'Como se usan los datos'."
        else -> "I have read and understood 'How data is used' above."
    }

    // 체크박스 3: 동의 철회 이해
    fun checkbox3Withdrawal(): String = when (getLang()) {
        "ko" -> "위 '동의 철회 방법'을 읽고 이해했습니다."
        "ja" -> "上記の'同意撤回方法'を読んで理解しました。"
        "zh" -> "我已阅读并理解上述'撤回同意的方法'。"
        "es" -> "He leido y comprendido 'Como revocar el consentimiento'."
        else -> "I have read and understood 'How to withdraw consent' above."
    }

    // 체크박스 4: 최종 동의
    fun checkbox4FinalConsent(): String = when (getLang()) {
        "ko" -> "AccessibilityService API 사용에 동의합니다."
        "ja" -> "AccessibilityService APIの使用に同意します。"
        "zh" -> "我同意使用AccessibilityService API。"
        "es" -> "Acepto el uso de AccessibilityService API."
        else -> "I agree to the use of AccessibilityService API."
    }

    // 버튼 - 동의하고 설정으로 이동
    fun agreeButton(): String = when (getLang()) {
        "ko" -> "동의하고 설정으로 이동"
        "ja" -> "同意して設定へ移動"
        "zh" -> "同意并前往设置"
        "es" -> "Aceptar e ir a Ajustes"
        else -> "Agree and Go to Settings"
    }

    fun declineButton(): String = when (getLang()) {
        "ko" -> "동의하지 않습니다"
        "ja" -> "同意しません"
        "zh" -> "我不同意"
        "es" -> "No acepto"
        else -> "I Decline"
    }

    fun declinedTitle(): String = when (getLang()) {
        "ko" -> "앱을 사용할 수 없습니다"
        "ja" -> "アプリを使用できません"
        "zh" -> "无法使用应用"
        "es" -> "No se puede usar la app"
        else -> "Cannot Use App"
    }

    fun declinedMessage(): String = when (getLang()) {
        "ko" -> "접근성 서비스 권한은 rebon의 핵심 앱 차단 기능에 필수입니다.\n\n" +
                "Android에서 현재 실행 중인 앱을 감지할 수 있는 유일한 방법이기 때문입니다.\n\n" +
                "이 권한 없이는 앱 차단 기능이 작동하지 않습니다."
        "ja" -> "アクセシビリティサービス権限は、rebonの核心的なアプリブロック機能に必須です。\n\n" +
                "Androidで現在実行中のアプリを検出する唯一の方法だからです。\n\n" +
                "この権限なしではアプリブロック機能が動作しません。"
        "zh" -> "无障碍服务权限是rebon核心应用阻止功能的必需条件。\n\n" +
                "因为这是Android中检测当前运行应用的唯一方法。\n\n" +
                "没有此权限，应用阻止功能将无法工作。"
        "es" -> "El permiso del Servicio de Accesibilidad es esencial para la funcion de bloqueo de apps de rebon.\n\n" +
                "Es el unico metodo en Android para detectar la app en ejecucion.\n\n" +
                "Sin este permiso, la funcion de bloqueo de apps no funcionara."
        else -> "Accessibility Service permission is essential for rebon's core app blocking feature.\n\n" +
                "It is the only method in Android to detect the currently running app.\n\n" +
                "Without this permission, the app blocking feature will not work."
    }

    fun understand(): String = when (getLang()) {
        "ko" -> "확인"
        "ja" -> "了解"
        "zh" -> "确定"
        "es" -> "Entendido"
        else -> "OK"
    }

    // ===== Compact versions (for fitting on one page) =====

    fun whyNeededCompact(): String = when (getLang()) {
        "ko" -> "왜 필요한가요?\nrebon은 설정한 앱 사용을 제한합니다. Android에서 실행 중인 앱 감지는 접근성 서비스만 가능합니다."
        "ja" -> "なぜ必要?\nrebonは設定アプリの使用を制限します。Androidでアプリ検出はアクセシビリティのみ可能です。"
        "zh" -> "为什么需要?\nrebon限制设置的应用使用。Android中只有无障碍服务可检测运行应用。"
        "es" -> "Por que?\nrebon restringe apps configuradas. Solo Accesibilidad detecta apps en Android."
        else -> "Why needed?\nrebon restricts configured apps. Only Accessibility Service can detect running apps on Android."
    }

    fun featuresCompact(): String = when (getLang()) {
        "ko" -> "기능: 현재 앱 패키지명 감지 - 차단 앱이면 홈으로 이동"
        "ja" -> "機能: アプリ名検出 - ブロック対象ならホームへ"
        "zh" -> "功能: 检测应用包名 - 被阻止则返回主屏"
        "es" -> "Funcion: Detecta app - Si bloqueada, va a inicio"
        else -> "Function: Detect app name - If blocked, go to home"
    }

    fun notCollectCompact(): String = when (getLang()) {
        "ko" -> "수집 안함: 화면 내용, 비밀번호, 사용 기록 없음. 외부 전송/공유 없음. 기기 내 처리만."
        "ja" -> "収集なし: 画面内容、パスワード、履歴なし。外部送信/共有なし。デバイス内処理のみ。"
        "zh" -> "不收集: 屏幕内容、密码、记录。不外传/共享。仅设备处理。"
        "es" -> "No recopila: contenido, contraseñas, historial. Sin envío externo. Solo local."
        else -> "No collection: screen content, passwords, history. No external transfer. Device-only processing."
    }
}

@Composable
fun TutorialScreen(
    preferenceManager: PreferenceManager?,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    var currentStep by remember { mutableIntStateOf(0) }

    val totalSteps = 10 // 전체 단계 수

    Box(modifier = Modifier.fillMaxSize()) {
        // 각 스텝 렌더링
        when (currentStep) {
            0 -> WelcomeStep(
                hapticManager = hapticManager,
                onNext = {
                    hapticManager.lightOn()
                    currentStep = 1
                }
            )
            1 -> PermissionStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 2 }
            )
            2 -> FitnessAppConnectionTutorialStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 3 }
            )
            // Step 3: 접근성 권한 동의 (Google Play 정책 준수)
            // 4개 체크박스 + 동의 후 바로 설정 이동 + 권한 확인
            3 -> AccessibilityConsentStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onAgree = { currentStep = 4 },  // 권한 확인 완료 후 앱 선택으로 바로 이동
                onDecline = { }  // 거부 시 다이얼로그만 표시 (진행 안함)
            )
            // Step 31: 제거됨 - AccessibilityConsentStep에서 동의+설정+확인 모두 처리
            4 -> AppSelectionStep(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 5 }
            )
            5 -> TestBlockingStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 6 }
            )
            6 -> GoalInputStep(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 7 }
            )
            7 -> WalkingStep(
                preferenceManager = preferenceManager,
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 8 }
            )
            8 -> UnlockedStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = { currentStep = 9 }
            )
            9 -> EmergencyButtonStep(
                hapticManager = hapticManager,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = {
                    hapticManager.goalAchieved()
                    preferenceManager?.setTutorialCompleted(true)
                    currentStep = 10
                }
            )
            10 -> {
                onComplete()
            }
        }
    }
}

/**
 * 튜토리얼 프로그레스바 컴포넌트
 */
@Composable
fun TutorialProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentStep.toFloat() / totalSteps).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.setup_progress),
                fontSize = StandTypography.labelLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "$currentStep / $totalSteps",
                fontSize = StandTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = StandColors.AccentPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 프로그레스바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                StandColors.WarmLightDim,
                                StandColors.WarmLight,
                                StandColors.WarmLightBright
                            )
                        )
                    )
            )
        }
    }
}

// 1. 환영 화면 - 프리미엄 피트니스 스타일
@Composable
fun WelcomeStep(hapticManager: HapticManager? = null, onNext: () -> Unit) {
    // 프리미엄 색상
    val TealPrimary = Color(0xFF00BFA5)
    val TealDark = Color(0xFF008E76)
    val NavyDark = Color(0xFF0D1B2A)
    val NavyMid = Color(0xFF1B263B)
    val BottomSheetBg = Color(0xFF0A0A0A)

    // 페이드인 애니메이션
    var isVisible by remember { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "fadeAlpha"
    )
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
        hapticManager?.lightOn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BottomSheetBg)
    ) {
        // 상단 70% - Teal 그라데이션 배경
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(TealPrimary, TealDark, NavyMid, NavyDark),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .offset(y = (-slideOffset).dp)
                    .alpha(fadeAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 로고/아이콘
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text("🏃", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "rebon",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.stand_up),
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = stringResource(R.string.stand_up_desc),
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            }
        }

        // 하단 바텀 시트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(BottomSheetBg)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 72.dp)
                .alpha(fadeAlpha)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.sitting_to_walking),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.goal_unlocks_apps),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 시작 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.get_started),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TealPrimary)
                            .clickable(enabled = isVisible) {
                                hapticManager?.heavyClick()
                                onNext()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("→", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. 권한 요청
@Composable
fun PermissionStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 1,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(true) }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activityPermissionGranted = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로그레스바
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.permission_settings),
                    fontSize = StandTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.rebon_needs_permissions),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(48.dp))

        PermissionCard(
            title = "🚶 " + stringResource(R.string.step_measurement_title),
            description = stringResource(R.string.step_count_permission),
            isGranted = activityPermissionGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = "🔔 " + stringResource(R.string.notification_title),
                description = stringResource(R.string.notification_permission),
                isGranted = notificationPermissionGranted,
                onRequest = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                hapticManager?.success()
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = activityPermissionGranted
        ) {
            Text(stringResource(R.string.next), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
        }
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MockupColors.Blue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = StandTypography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            if (isGranted) {
                Text(
                    text = "✓",
                    fontSize = StandTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            } else {
                Button(onClick = onRequest) {
                    Text(stringResource(R.string.allow))
                }
            }
        }
    }
}

// 3. 피트니스 앱 연결 (튜토리얼)
@Composable
fun FitnessAppConnectionTutorialStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 2,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()

    var installedApps by remember { mutableStateOf<List<FitnessApp>>(emptyList()) }
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.createPermissionRequestContract()
    ) { granted ->
        scope.launch {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                isConnecting = false
                // Health Connect 연결 설정 저장
                val firstApp = installedApps.firstOrNull()
                preferenceManager.setUseHealthConnect(true)
                preferenceManager.setHealthConnectConnected(true)
                preferenceManager.setConnectedFitnessAppName(firstApp?.appName ?: "")
                // 서비스 재시작
                StepCounterService.stop(context)
                StepCounterService.start(context)
                // 자동으로 다음 단계로
                delay(500)
                onNext()
            } else {
                isConnecting = false
            }
        }
    }

    // 초기화
    LaunchedEffect(Unit) {
        // Health Connect 사용 가능 여부 체크
        isHealthConnectAvailable = healthConnectManager.isAvailable()

        // 설치된 피트니스 앱 목록은 항상 가져오기 (Health Connect 여부와 무관)
        installedApps = healthConnectManager.getInstalledFitnessApps()

        // Health Connect 사용 가능하면 권한 체크
        if (isHealthConnectAvailable) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            // 이미 권한이 있으면 자동으로 다음으로
            if (hasPermissions) {
                delay(1000)
                onNext()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏃",
                    fontSize = StandTypography.displayLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.fitness_app_connection),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.fitness_connection_desc),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(48.dp))

        // 설치된 앱이 있으면 (Health Connect 여부와 상관없이)
        if (installedApps.isNotEmpty()) {
            Text(
                text = stringResource(R.string.detected_fitness_apps),
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            installedApps.forEach { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = app.icon, fontSize = StandTypography.headlineSmall)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = app.appName, fontSize = StandTypography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = stringResource(R.string.installed), fontSize = StandTypography.labelMedium, color = MockupColors.TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Connect 사용 가능 여부에 따라 버튼 변경
            if (isHealthConnectAvailable) {
                Button(
                    onClick = {
                        isConnecting = true
                        permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = installedApps.firstOrNull()?.color ?: MockupColors.Blue
                    )
                ) {
                    Text(
                        text = if (isConnecting) stringResource(R.string.connecting) else stringResource(R.string.connect_with_app, installedApps.firstOrNull()?.appName ?: ""),
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Health Connect 없으면 설치 안내
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MockupColors.TextMuted.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.health_connect_required_title),
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.health_connect_required, installedApps.firstOrNull()?.appName ?: ""),
                            fontSize = StandTypography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { healthConnectManager.openHealthConnectPlayStore() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MockupColors.TextMuted
                            )
                        ) {
                            Text(stringResource(R.string.install_from_play_store))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.android_9_required),
                            fontSize = StandTypography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        // 설치된 앱이 없으면
        else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = StandColors.WarmLight.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.recommended_fitness_apps),
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.WarmLightBright
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.install_fitness_app_desc),
                        fontSize = StandTypography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 추천 앱 목록
                    listOf(
                        stringResource(R.string.samsung_health),
                        "Google Fit"
                        // "Garmin Connect", // 테스트 미완료
                        // "Fitbit"          // 테스트 미완료
                    ).forEach { appName ->
                        Text(
                            text = "• $appName",
                            fontSize = StandTypography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.connect_later_in_settings),
                    fontSize = StandTypography.labelLarge,
                    color = StandColors.WarmLight
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 나중에 하기 버튼
        TextButton(
            onClick = {
                hapticManager?.click()
                onNext()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.do_later_use_basic_sensor), color = Color.White.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.basic_sensor_note),
            fontSize = StandTypography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
            }
        }
    }
}

// 3.5 접근성 권한 동의 (Google Play 정책 준수 - 체크박스 4개 명시적 동의 필수)
// Google Play 정책 요구사항:
// 1. 체크박스 4개로 명시적 동의 (버튼만으로는 불충분)
// 2. 접근성 API 사용 목적 상세 설명 (Google 권장 형식)
// 3. 뒤로가기로 우회 불가
// 4. 동의 후 바로 설정으로 이동 + 권한 확인
@Composable
fun AccessibilityConsentStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 3,
    totalSteps: Int = 10,
    onAgree: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current
    var showDeclineDialog by remember { mutableStateOf(false) }

    // 4개의 체크박스 상태
    var check1DataCollection by remember { mutableStateOf(false) }
    var check2DataUsage by remember { mutableStateOf(false) }
    var check3Withdrawal by remember { mutableStateOf(false) }
    var check4FinalConsent by remember { mutableStateOf(false) }
    val allChecked = check1DataCollection && check2DataUsage && check3Withdrawal && check4FinalConsent

    // 접근성 서비스 활성화 확인 (설정에서 돌아왔을 때)
    LaunchedEffect(Unit) {
        while (true) {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (enabledServices?.contains("com.moveoftoday.walkorwait") == true) {
                hapticManager?.success()
                kotlinx.coroutines.delay(500)
                onAgree()
                break
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // Google Play 정책: 뒤로가기로 동의 화면을 우회할 수 없음
    androidx.activity.compose.BackHandler(enabled = true) {
        showDeclineDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            // 스크롤 가능한 컨텐츠 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 제목
                Text(
                    text = TutorialAccessibilityStrings.title(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Google 권장 형식 공개 문구 (노란색 강조)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = TutorialAccessibilityStrings.prominentDisclosure(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MockupColors.TextPrimary,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4개 섹션 설명
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 섹션 1: 수집하는 데이터
                        TutorialDisclosureSection(
                            title = TutorialAccessibilityStrings.sectionDataCollected(),
                            content = TutorialAccessibilityStrings.dataCollectedContent()
                        )

                        // 섹션 2: 데이터 사용 방법
                        TutorialDisclosureSection(
                            title = TutorialAccessibilityStrings.sectionDataUsage(),
                            content = TutorialAccessibilityStrings.dataUsageContent()
                        )

                        // 섹션 3: 수집하지 않는 데이터
                        TutorialDisclosureSection(
                            title = TutorialAccessibilityStrings.sectionDataNotCollected(),
                            content = TutorialAccessibilityStrings.dataNotCollectedContent()
                        )

                        // 섹션 4: 동의 철회 방법
                        TutorialDisclosureSection(
                            title = TutorialAccessibilityStrings.sectionWithdrawal(),
                            content = TutorialAccessibilityStrings.withdrawalContent()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4개 체크박스 (Google Play 정책: 명시적 동의)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TutorialConsentCheckbox(
                        checked = check1DataCollection,
                        onCheckedChange = { check1DataCollection = it },
                        label = TutorialAccessibilityStrings.checkbox1DataCollection()
                    )
                    TutorialConsentCheckbox(
                        checked = check2DataUsage,
                        onCheckedChange = { check2DataUsage = it },
                        label = TutorialAccessibilityStrings.checkbox2DataUsage()
                    )
                    TutorialConsentCheckbox(
                        checked = check3Withdrawal,
                        onCheckedChange = { check3Withdrawal = it },
                        label = TutorialAccessibilityStrings.checkbox3Withdrawal()
                    )
                    TutorialConsentCheckbox(
                        checked = check4FinalConsent,
                        onCheckedChange = { check4FinalConsent = it },
                        label = TutorialAccessibilityStrings.checkbox4FinalConsent()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 하단 고정 버튼 - Google Play 정책: 두 개 버튼 필수 (동의/거부)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 동의 안함 버튼
                OutlinedButton(
                    onClick = {
                        hapticManager?.click()
                        showDeclineDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = TutorialAccessibilityStrings.declineButton(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // 동의하고 설정으로 이동 버튼 (모든 체크박스 선택 시에만 활성화)
                Button(
                    onClick = {
                        if (allChecked) {
                            hapticManager?.success()
                            // 접근성 설정 복귀 플래그 설정
                            PreferenceManager(context).setAwaitingAccessibilityReturn(true)
                            // 접근성 설정으로 바로 이동
                            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    },
                    enabled = allChecked,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = TutorialAccessibilityStrings.agreeButton(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allChecked) Color.Black else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // 뒤로가기/거부 시 표시되는 다이얼로그
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = {
                Text(
                    text = TutorialAccessibilityStrings.declinedTitle(),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = TutorialAccessibilityStrings.declinedMessage(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeclineDialog = false
                    }
                ) {
                    Text(TutorialAccessibilityStrings.understand())
                }
            }
        )
    }
}

// 섹션 표시용 헬퍼 컴포저블
@Composable
private fun TutorialDisclosureSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = content,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f),
            lineHeight = 14.sp
        )
    }
}

// 체크박스 헬퍼 컴포저블
@Composable
private fun TutorialConsentCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (checked) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = if (checked) Color.White else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Color.White.copy(alpha = 0.5f),
                checkmarkColor = Color.Black
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            lineHeight = 14.sp
        )
    }
}

// 4. 접근성 설정 (rebon ON - 필수!)
@Composable
fun AccessibilityStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 3,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(isChecking) {
        if (isChecking) {
            while (true) {
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )

                if (enabledServices?.contains("com.moveoftoday.walkorwait") == true) {
                    hapticManager?.success()
                    delay(1000)
                    onNext()
                    break
                }

                delay(1000)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.app_control_settings),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.app_control_ready_desc),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MockupColors.TextMuted.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚙️ " + stringResource(R.string.how_to_setup),
                            fontSize = StandTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.accessibility_setup_steps),
                            fontSize = StandTypography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        // 접근성 설정 후 앱으로 자동 복귀하기 위한 플래그 설정
                        PreferenceManager(context).setAwaitingAccessibilityReturn(true)
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MockupColors.TextMuted
                    )
                ) {
                    Text(stringResource(R.string.go_to_settings), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.rebon_on_required),
                    fontSize = StandTypography.labelLarge,
                    color = MockupColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 4. 앱 선택
@Composable
fun AppSelectionStep(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    currentStep: Int = 4,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    // 모든 설치된 앱 가져오기 (카테고리 필터링 제거)
    val appsByCategory = remember {
        val allApps = AppUtils.getInstalledAppsByCategory(context)

        // 디버그: 설치된 앱 개수 확인
        android.util.Log.d("TutorialScreen", "Total categories: ${allApps.size}")
        allApps.forEach { (category, apps) ->
            android.util.Log.d("TutorialScreen", "$category: ${apps.size} apps")
        }

        // 모든 앱 반환 (필터링 제거)
        allApps
    }

    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var expandedCategories by remember { mutableStateOf(setOf<AppCategory>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_apps_to_control),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.select_apps_desc),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(24.dp))

        // 앱이 없는 경우 안내 메시지
        val totalApps = appsByCategory.values.flatten().size
        if (totalApps == 0) {
            EmptyState(
                icon = "📱",
                title = stringResource(R.string.no_apps_to_control),
                description = stringResource(R.string.no_apps_install_guide),
                modifier = Modifier.weight(1f)
            )
        } else {
            // 선택된 앱 개수 표시
            if (selectedApps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.PrimaryLight
                    )
                ) {
                    Text(
                        text = stringResource(R.string.apps_selected_count, selectedApps.size),
                        fontSize = StandTypography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.Primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 스크롤 가능한 앱 목록
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
            appsByCategory.forEach { (category, apps) ->
                // 카테고리 헤더
                item(key = "header_$category") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            expandedCategories = if (category in expandedCategories) {
                                expandedCategories - category
                            } else {
                                expandedCategories + category
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${getCategoryIcon(category)} ${stringResource(category.displayNameRes)}",
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.category_apps_count, apps.size),
                                fontSize = StandTypography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 카테고리가 펼쳐져 있으면 앱 목록 표시
                if (category in expandedCategories) {
                    items(
                        items = apps,
                        key = { app -> app.packageName }
                    ) { app ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                app.icon?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = app.appName,
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                Checkbox(
                                    checked = selectedApps.contains(app.packageName),
                                    onCheckedChange = { checked ->
                                        selectedApps = if (checked) {
                                            selectedApps + app.packageName
                                        } else {
                                            selectedApps - app.packageName
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                hapticManager?.success()
                preferenceManager?.saveLockedApps(selectedApps)
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedApps.isNotEmpty() || totalApps == 0
        ) {
            Text(
                text = if (totalApps == 0) stringResource(R.string.skip) else stringResource(R.string.next_with_count, selectedApps.size),
                fontSize = StandTypography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
            }
        }
    }
}

// 카테고리별 아이콘
private fun getCategoryIcon(category: AppCategory): String {
    return when (category) {
        AppCategory.GAME -> "🎮"
        AppCategory.VIDEO -> "🎬"
        AppCategory.SOCIAL -> "💬"
        AppCategory.MUSIC_AUDIO -> "🎵"
        AppCategory.ENTERTAINMENT -> "🎪"
        AppCategory.PRODUCTIVITY -> "💼"
        AppCategory.COMMUNICATION -> "📱"
        AppCategory.SHOPPING -> "🛒"
        AppCategory.OTHER -> "📦"
    }
}

// 5. 차단 체험 - 조명이 꺼지는 효과
@Composable
fun TestBlockingStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 5,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var hasLeftApp by remember { mutableStateOf(false) }
    var canProceed by remember { mutableStateOf(false) }

    // 깜빡이는 애니메이션 (차단 상태)
    val infiniteTransition = rememberInfiniteTransition(label = "blockBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    DisposableEffect(Unit) {
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    hasLeftApp = true
                }
                else -> {}
            }
        }

        lifecycleOwner?.lifecycle?.addObserver(observer)

        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    // 백그라운드 갔다온 후 5초 대기
    LaunchedEffect(hasLeftApp) {
        if (hasLeftApp) {
            delay(5000) // 5초 대기
            hapticManager?.success()
            canProceed = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 어두운 배경 + 깜빡이는 효과 (차단 상태)
        if (!canProceed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLightDim.copy(alpha = blinkAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 600f
                        )
                    )
            )
        } else {
            // 성공 시 밝아지는 효과
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLight.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            radius = 800f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 꺼진 전구 아이콘 (차단 상태)
                Box(contentAlignment = Alignment.Center) {
                    if (!canProceed) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .alpha(blinkAlpha * 0.5f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            StandColors.WarmLightDim.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                    Text(
                        text = if (canProceed) "💡" else "🔒",
                        fontSize = StandTypography.displayLarge,
                        modifier = Modifier.alpha(if (canProceed) 1f else blinkAlpha + 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (canProceed) stringResource(R.string.experience_complete) else stringResource(R.string.app_blocked),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canProceed) StandColors.WarmLightBright else StandColors.WarmLightDim
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (canProceed)
                        stringResource(R.string.blocking_experienced_desc)
                    else
                        stringResource(R.string.try_running_app_desc),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canProceed)
                            StandColors.WarmLight.copy(alpha = 0.15f)
                        else
                            StandColors.WarmLightDim.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        when {
                            canProceed -> {
                                Text(
                                    text = stringResource(R.string.experience_complete_emoji),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightBright
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.lets_walk_to_turn_on),
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            hasLeftApp -> {
                                Text(
                                    text = stringResource(R.string.checking),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightDim
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.checking_blocking_desc),
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = stringResource(R.string.try_running_app_emoji),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLightDim
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.blocking_test_steps),
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (canProceed) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StandColors.WarmLight
                        )
                    ) {
                        Text(stringResource(R.string.next), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                    }
                } else if (hasLeftApp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = StandColors.WarmLightDim
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.proceed_soon),
                        fontSize = StandTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Button(
                        onClick = { /* 비활성화 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false
                    ) {
                        Text(stringResource(R.string.try_running_app), fontSize = StandTypography.titleSmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.must_run_blocked_app),
                        fontSize = StandTypography.labelLarge,
                        color = StandColors.WarmLightDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 6. 목표 입력
@Composable
fun GoalInputStep(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    currentStep: Int = 6,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }

    var selectedUnit by remember { mutableStateOf("steps") } // "steps" or "km"
    var hasHealthConnectPermission by remember { mutableStateOf(false) }

    // 슬라이더 값 (걸음: 50-70, km: 0.04-0.1)
    var stepsSliderValue by remember { mutableFloatStateOf(60f) } // 기본값 60보
    var kmSliderValue by remember { mutableFloatStateOf(0.07f) } // 기본값 0.07km

    // Health Connect 권한 확인
    LaunchedEffect(Unit) {
        hasHealthConnectPermission = healthConnectManager.isAvailable() && healthConnectManager.hasAllPermissions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.goal_setting),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.set_goal_for_experience),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 24.sp
                )

        Spacer(modifier = Modifier.height(32.dp))

        // 단위 선택
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUnit == "steps")
                        StandColors.WarmLight.copy(alpha = 0.2f)
                    else
                        Color.White.copy(alpha = 0.1f)
                ),
                border = if (selectedUnit == "steps")
                    androidx.compose.foundation.BorderStroke(2.dp, StandColors.WarmLight)
                else
                    null,
                onClick = { selectedUnit = "steps" }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.step_count),
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUnit == "steps") StandColors.WarmLightBright else Color.White
                    )
                    Text(
                        text = stringResource(R.string.basic_sensor),
                        fontSize = StandTypography.labelLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUnit == "km")
                        StandColors.WarmLight.copy(alpha = 0.2f)
                    else if (!hasHealthConnectPermission)
                        Color.White.copy(alpha = 0.05f)
                    else
                        Color.White.copy(alpha = 0.1f)
                ),
                border = if (selectedUnit == "km")
                    androidx.compose.foundation.BorderStroke(2.dp, StandColors.WarmLight)
                else
                    null,
                onClick = {
                    if (hasHealthConnectPermission) {
                        selectedUnit = "km"
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.distance_km),
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUnit == "km")
                            StandColors.WarmLightBright
                        else if (!hasHealthConnectPermission)
                            Color.White.copy(alpha = 0.4f)
                        else
                            Color.White
                    )
                    Text(
                        text = if (hasHealthConnectPermission) stringResource(R.string.fitness_connected) else stringResource(R.string.connection_required),
                        fontSize = StandTypography.labelLarge,
                        color = if (hasHealthConnectPermission) Color.White.copy(alpha = 0.6f) else MockupColors.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 슬라이더로 목표 설정
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 선택된 값 표시
            Text(
                text = if (selectedUnit == "steps") {
                    stringResource(R.string.steps_format, stepsSliderValue.toInt())
                } else {
                    stringResource(R.string.km_format, kmSliderValue)
                },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = StandColors.WarmLightBright
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedUnit == "steps") stringResource(R.string.steps_range) else stringResource(R.string.km_range),
                fontSize = StandTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 슬라이더
            if (selectedUnit == "steps") {
                Slider(
                    value = stepsSliderValue,
                    onValueChange = { stepsSliderValue = it },
                    valueRange = 50f..70f,
                    steps = 19, // 50-70 사이 20개 값 (1보 단위)
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = StandColors.WarmLightBright,
                        activeTrackColor = StandColors.WarmLight,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            } else {
                Slider(
                    value = kmSliderValue,
                    onValueChange = { kmSliderValue = it },
                    valueRange = 0.04f..0.1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = StandColors.WarmLightBright,
                        activeTrackColor = StandColors.WarmLight,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = StandColors.WarmLight.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelIcon(iconName = "icon_light_bulb", size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.when_goal_achieved),
                        fontSize = StandTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.WarmLightBright
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.blocked_apps_unlock_desc),
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }

        // km 선택 시 Health Connect 안내
        if (selectedUnit == "km" && !hasHealthConnectPermission) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MockupColors.TextMuted.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.fitness_required_for_km),
                        fontSize = StandTypography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.km_requires_fitness),
                        fontSize = StandTypography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                hapticManager?.success()
                if (selectedUnit == "steps") {
                    val steps = stepsSliderValue.toInt()
                    // 목표 설정 (걸음 수)
                    preferenceManager?.saveGoal(steps)
                    preferenceManager?.saveGoalUnit("steps")
                    onNext()
                } else { // km
                    val km = kmSliderValue.toDouble()
                    // km를 걸음 수로 변환 (1km ≈ 1300보)
                    val steps = (km * 1300).toInt()
                    preferenceManager?.saveGoal(steps)
                    preferenceManager?.saveGoalUnit("km")
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = true // 슬라이더는 항상 유효한 값
        ) {
            Text(stringResource(R.string.next), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold)
        }
            }
        }
    }
}

// 7. 걷기 체험 - 걸음마다 불이 켜지는 애니메이션
@Composable
fun WalkingStep(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager? = null,
    currentStep: Int = 7,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as WalkorWaitApp
    val repository = app.userDataRepository

    // 목표 단위 확인 (steps or km)
    val goalUnit = remember { preferenceManager?.getGoalUnit() ?: "steps" }
    val isKmMode = goalUnit == "km"

    // 튜토리얼 시작 시점의 걸음 수를 기록 (Health Connect 덮어쓰기 방지)
    val baselineSteps = remember { repository.getTodaySteps() }
    var currentSteps by remember { mutableIntStateOf(0) }
    val targetSteps = repository.getGoal()
    var hasLeftApp by remember { mutableStateOf(false) }
    var goalJustAchieved by remember { mutableStateOf(false) }
    var previousSteps by remember { mutableIntStateOf(0) }

    // 걸음 감지 시 불빛 깜빡임 애니메이션
    var stepFlash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (stepFlash) 1f else 0.3f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "flashAlpha",
        finishedListener = { stepFlash = false }
    )

    // 목표 달성 시 빛나는 효과
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        while (true) {
            val rawSteps = repository.getTodaySteps()
            // 튜토리얼 시작 시점부터의 걸음 수만 계산
            val newSteps = maxOf(0, rawSteps - baselineSteps)
            val wasAchieved = currentSteps >= targetSteps

            // 걸음 수가 증가하면 불빛 깜빡임
            if (newSteps > previousSteps && newSteps < targetSteps) {
                stepFlash = true
                hapticManager?.lightOn()
            }
            previousSteps = currentSteps
            currentSteps = newSteps
            val isNowAchieved = currentSteps >= targetSteps

            // 목표 달성 순간 햅틱
            if (isNowAchieved && !wasAchieved && !goalJustAchieved) {
                hapticManager?.goalAchieved()
                goalJustAchieved = true
            }
            delay(1000)
        }
    }
    DisposableEffect(Unit) {
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    hasLeftApp = true
                }
                else -> {}
            }
        }

        lifecycleOwner?.lifecycle?.addObserver(observer)

        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val isGoalAchieved = currentSteps >= targetSteps

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 배경 빛 효과
        if (isGoalAchieved) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(glowAlpha * 0.4f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLightBright.copy(alpha = 0.6f),
                                StandColors.WarmLight.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 1000f
                        )
                    )
            )
        } else if (stepFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(flashAlpha * 0.3f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StandColors.WarmLight.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            radius = 800f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // km 모드일 때 거리로 표시
                val targetDisplay = if (isKmMode) {
                    String.format("%.2fkm", targetSteps / 1300.0)
                } else {
                    TutorialStrings.formatStepsTarget(targetSteps)
                }

                Text(
                    text = if (isGoalAchieved && !hasLeftApp)
                        stringResource(R.string.goal_achieved_try_app)
                    else if (isGoalAchieved && hasLeftApp)
                        stringResource(R.string.experience_complete)
                    else
                        stringResource(R.string.walk_x_steps, targetDisplay),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    color = if (isGoalAchieved) StandColors.WarmLightBright else Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 불빛 아이콘 Row (걸음 수에 따라 켜짐)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val lightsCount = targetSteps.coerceAtMost(10)
                    val litLights = ((currentSteps.toFloat() / targetSteps) * lightsCount).toInt()

                    for (i in 0 until lightsCount) {
                        val isLit = i < litLights
                        Box(
                            modifier = Modifier
                                .size(if (isLit) 28.dp else 24.dp)
                                .padding(2.dp)
                                .background(
                                    brush = if (isLit) Brush.radialGradient(
                                        colors = listOf(
                                            StandColors.WarmLightBright,
                                            StandColors.WarmLight.copy(alpha = 0.6f),
                                            Color.Transparent
                                        )
                                    ) else Brush.radialGradient(
                                        colors = listOf(
                                            Color.Gray.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLit) {
                                Text(
                                    text = "💡",
                                    fontSize = 14.sp,
                                    modifier = Modifier.alpha(if (i == litLights - 1 && stepFlash) flashAlpha else 1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 걸음 수 표시 (빛나는 효과)
                Box(contentAlignment = Alignment.Center) {
                    if (isGoalAchieved) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .alpha(glowAlpha * 0.5f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            StandColors.WarmLightBright.copy(alpha = 0.8f),
                                            StandColors.WarmLight.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // km 모드일 때 거리로 표시
                        val currentDisplay = if (isKmMode) {
                            String.format("%.2f", currentSteps / 1300.0)
                        } else {
                            currentSteps.toString()
                        }
                        val targetDisplaySmall = if (isKmMode) {
                            String.format("%.2f km", targetSteps / 1300.0)
                        } else {
                            TutorialStrings.formatStepsTarget(targetSteps)
                        }

                        Text(
                            text = currentDisplay,
                            fontSize = StandTypography.displayHero,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoalAchieved) StandColors.WarmLightBright else StandColors.WarmLight
                        )
                        Text(
                            text = "/ $targetDisplaySmall",
                            fontSize = StandTypography.headlineSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 프로그레스바 (따뜻한 조명 그라데이션)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (isGoalAchieved) listOf(
                                        StandColors.WarmLight,
                                        StandColors.WarmLightBright
                                    ) else listOf(
                                        StandColors.WarmLightDim,
                                        StandColors.WarmLight
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isGoalAchieved && hasLeftApp -> StandColors.WarmLight.copy(alpha = 0.15f)
                            isGoalAchieved -> StandColors.WarmLight.copy(alpha = 0.1f)
                            else -> StandColors.WarmLightDim.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        when {
                            isGoalAchieved && hasLeftApp -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PixelIcon(iconName = "icon_trophy", size = 18.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.perfect),
                                        fontSize = StandTypography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = StandColors.WarmLightBright
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.goal_achieved_app_tested_desc),
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            isGoalAchieved -> {
                                Text(
                                    text = stringResource(R.string.try_running_app_now),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLight
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.app_unlock_guide),
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = stringResource(R.string.walk_emoji),
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = StandColors.WarmLight
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.walk_with_phone_desc),
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // 테스트 버튼들 (튜토리얼용)
                if (!isGoalAchieved) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = StandColors.WarmLightDim.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.test_tools),
                                fontSize = StandTypography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLight
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        val newSteps = currentSteps + 5
                                        repository.saveTodaySteps(newSteps)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StandColors.WarmLightDim
                                    )
                                ) {
                                    Text("+5", fontSize = StandTypography.bodyMedium, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        repository.saveTodaySteps(targetSteps)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StandColors.WarmLight
                                    )
                                ) {
                                    Text(stringResource(R.string.achieve), fontSize = StandTypography.bodyMedium, color = StandColors.DarkBackground)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isGoalAchieved && hasLeftApp) {
                    Button(
                        onClick = {
                            // 다음 단계로 이동 (걸음 수는 UnlockedStep에서 리셋)
                            onNext()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StandColors.WarmLight
                        )
                    ) {
                        Text(stringResource(R.string.next), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                    }
                } else {
                    Button(
                        onClick = { /* 비활성화 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false
                    ) {
                        Text(
                            text = if (!isGoalAchieved) stringResource(R.string.steps_required) else stringResource(R.string.app_run_required),
                            fontSize = StandTypography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (!isGoalAchieved)
                            stringResource(R.string.fill_steps)
                        else
                            stringResource(R.string.try_running_app_short),
                        fontSize = StandTypography.labelLarge,
                        color = StandColors.WarmLightDim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 8. 해제 확인 - 조명이 밝아지는 효과
@Composable
fun UnlockedStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 8,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    // 빛나는 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "unlockGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 배경 빛 효과
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(glowAlpha * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.WarmLightBright.copy(alpha = 0.6f),
                            StandColors.WarmLight.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 빛나는 전구 아이콘
                Box(
                    modifier = Modifier.scale(glowScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .alpha(glowAlpha)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        StandColors.WarmLightBright.copy(alpha = 0.8f),
                                        StandColors.WarmLight.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "💡",
                        fontSize = StandTypography.displayLarge
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.goal_achieved_unlocked),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StandColors.WarmLightBright,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.goal_achieved_explanation),
                    fontSize = StandTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.WarmLight.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelIcon(iconName = "icon_light_bulb", size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.rebon_core),
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLightBright
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.daily_goal_explanation),
                            fontSize = StandTypography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        hapticManager?.success()
                        onNext()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StandColors.WarmLight
                    )
                ) {
                    Text(stringResource(R.string.next), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                }
            }
        }
    }
}

// 9. 휴식 버튼 설명
@Composable
fun EmergencyButtonStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 9,
    totalSteps: Int = 10,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.take_a_break),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = StandColors.WarmLight.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🕐",
                    fontSize = StandTypography.displaySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.break_15_min),
                    fontSize = StandTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = StandColors.WarmLightBright,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.break_features),
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIcon(iconName = "icon_light_bulb", size = 18.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.tip),
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = StandColors.WarmLight
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.break_description),
            fontSize = StandTypography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                hapticManager?.success()
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StandColors.WarmLight
            )
        ) {
            Text(stringResource(R.string.tutorial_complete), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
        }
            }
        }
    }
}

// 10. 크레딧 시스템 설명 - 따뜻한 조명 테마
@Composable
fun SubscriptionStep(
    hapticManager: HapticManager? = null,
    currentStep: Int = 10,
    totalSteps: Int = 11,
    onNext: () -> Unit
) {
    // 부드러운 빛나는 효과
    val infiniteTransition = rememberInfiniteTransition(label = "subscriptionGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StandColors.DarkBackground)
    ) {
        // 배경 조명 효과
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(glowAlpha * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            StandColors.WarmLight.copy(alpha = 0.5f),
                            StandColors.WarmLightDim.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutorialProgressBar(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 전구 아이콘
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .alpha(glowAlpha)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        StandColors.WarmLightBright.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "💡",
                        fontSize = StandTypography.displaySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = TutorialStrings.subscriptionPlan(),
                    fontSize = StandTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StandColors.WarmLightBright
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = TutorialStrings.autoPaymentAfterTrial(),
                    fontSize = StandTypography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 구독 플랜 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.WarmLight.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // 월간 플랜
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${UnicodeSymbols.CIRCLE_FILLED}", fontSize = StandTypography.titleMedium, color = StandColors.WarmLightBright)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(TutorialStrings.monthlySubscription(), fontSize = StandTypography.bodyLarge, color = Color.White)
                            }
                            Text(
                                text = TutorialStrings.monthlyPrice(),
                                fontSize = StandTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLightBright
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = StandColors.WarmLightDim.copy(alpha = 0.3f)
                        )

                        // 연간 플랜
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${UnicodeSymbols.STAR}", fontSize = StandTypography.titleMedium, color = StandColors.WarmLightBright)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(TutorialStrings.yearlySubscription(), fontSize = StandTypography.bodyLarge, color = Color.White)
                                    Text(TutorialStrings.twoMonthsFree(), fontSize = StandTypography.bodySmall, color = StandColors.WarmLight)
                                }
                            }
                            Text(
                                text = TutorialStrings.yearlyPrice(),
                                fontSize = StandTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLightBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // streak 방어 티켓 안내
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.WarmLightBright.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${UnicodeSymbols.SHIELD}", fontSize = StandTypography.headlineSmall)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = TutorialStrings.streakProtectionTicket(),
                                fontSize = StandTypography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = StandColors.WarmLightBright
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TutorialStrings.awardedByGoalRate(),
                            fontSize = StandTypography.bodyMedium,
                            color = StandColors.WarmLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("90%", fontSize = StandTypography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                Text(TutorialStrings.ticketCount(1), fontSize = StandTypography.bodyMedium, fontWeight = FontWeight.Bold, color = StandColors.WarmLight)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("95%", fontSize = StandTypography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                Text(TutorialStrings.ticketCount(2), fontSize = StandTypography.bodyMedium, fontWeight = FontWeight.Bold, color = StandColors.WarmLight)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("100%", fontSize = StandTypography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                Text(TutorialStrings.ticketCount(3), fontSize = StandTypography.bodyMedium, fontWeight = FontWeight.Bold, color = StandColors.WarmLightBright)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = TutorialStrings.subscriptionNote(),
                    fontSize = StandTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        hapticManager?.goalAchieved()
                        onNext()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StandColors.WarmLight
                    )
                ) {
                    Text(TutorialStrings.getStarted(), fontSize = StandTypography.titleSmall, fontWeight = FontWeight.Bold, color = StandColors.DarkBackground)
                }
            }
        }
    }
}