package com.moveoftoday.walkorwait.pet

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.moveoftoday.walkorwait.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.BillingManager
import com.moveoftoday.walkorwait.BuildConfig
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.PreferenceManager
import com.moveoftoday.walkorwait.HealthConnectManager
import com.moveoftoday.walkorwait.AppUtils
import com.moveoftoday.walkorwait.StepWidgetProvider
import com.moveoftoday.walkorwait.AppCategory
import com.moveoftoday.walkorwait.PromoCodeManager
import com.moveoftoday.walkorwait.SubscriptionManager
import com.moveoftoday.walkorwait.SubscriptionModel
import com.moveoftoday.walkorwait.AnalyticsManager
import com.moveoftoday.walkorwait.WalkorWaitApp
import com.moveoftoday.walkorwait.StepCounterService
import com.moveoftoday.walkorwait.GoogleSignInHelper
import com.moveoftoday.walkorwait.UnicodeSymbols
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.app.Activity
import android.util.Log

/**
 * Localization helper for PetTutorialScreen
 */
private object PetTutorialStrings {
    private fun getLang(): String = java.util.Locale.getDefault().language

    // ========== GoogleSignInStep ==========
    fun loggingIn(): String = when (getLang()) {
        "ko" -> "로그인 중..."
        "ja" -> "ログイン中..."
        "zh" -> "登录中..."
        "es" -> "Iniciando sesión..."
        else -> "Signing in..."
    }

    fun checkingData(): String = when (getLang()) {
        "ko" -> "데이터 확인 중..."
        "ja" -> "データ確認中..."
        "zh" -> "正在检查数据..."
        "es" -> "Verificando datos..."
        else -> "Checking data..."
    }

    fun dataRestoreComplete(): String = when (getLang()) {
        "ko" -> "데이터 복원 완료!"
        "ja" -> "データ復元完了!"
        "zh" -> "数据恢复完成!"
        "es" -> "¡Datos restaurados!"
        else -> "Data restored!"
    }

    fun loginComplete(): String = when (getLang()) {
        "ko" -> "로그인 완료!"
        "ja" -> "ログイン完了!"
        "zh" -> "登录成功!"
        "es" -> "¡Inicio de sesión exitoso!"
        else -> "Login complete!"
    }

    fun firebaseLoginFailed(): String = when (getLang()) {
        "ko" -> "Firebase 로그인 실패"
        "ja" -> "Firebaseログイン失敗"
        "zh" -> "Firebase登录失败"
        "es" -> "Error de inicio de sesión de Firebase"
        else -> "Firebase login failed"
    }

    // ========== FitnessConnectionStep ==========
    fun fitnessLoyalSpeech(): String = when (getLang()) {
        "ko" -> "피트니스 앱 연결해."
        "ja" -> "フィットネスアプリを接続して。"
        "zh" -> "连接健身应用。"
        "es" -> "Conecta la app de fitness."
        else -> "Connect fitness app."
    }

    fun fitnessTsundereSpeech(): String = when (getLang()) {
        "ko" -> "연결 안 해도 되긴 해..."
        "ja" -> "接続しなくてもいいけど..."
        "zh" -> "不连接也可以..."
        "es" -> "No tienes que conectar..."
        else -> "You don't have to connect..."
    }

    fun fitnessFoodieSpeech(): String = when (getLang()) {
        "ko" -> "피트니스 연결! 가보자고~"
        "ja" -> "フィットネス接続！行こう~"
        "zh" -> "连接健身！走起~"
        "es" -> "¡Conecta fitness! ¡Vamos~"
        else -> "Connect fitness! Let's go~"
    }

    fun fitnessPlayfulSpeech(): String = when (getLang()) {
        "ko" -> "피트니스 연결해봐"
        "ja" -> "フィットネス接続してみて"
        "zh" -> "试试连接健身吧"
        "es" -> "Prueba conectar fitness"
        else -> "Try connecting fitness"
    }

    fun fitnessTimidSpeech(): String = when (getLang()) {
        "ko" -> "연결하면 좋을 것 같아요..."
        "ja" -> "接続したら良いと思います..."
        "zh" -> "连接的话会更好..."
        "es" -> "Sería bueno conectar..."
        else -> "It would be good to connect..."
    }

    fun fitnessClumsySpeech(): String = when (getLang()) {
        "ko" -> "연결하면 더 정확해!"
        "ja" -> "接続するともっと正確！"
        "zh" -> "连接后更准确!"
        "es" -> "¡Más preciso si conectas!"
        else -> "More accurate if connected!"
    }

    fun fitnessAppConnection(): String = when (getLang()) {
        "ko" -> "피트니스 앱 연결"
        "ja" -> "フィットネスアプリ接続"
        "zh" -> "连接健身应用"
        "es" -> "Conectar app de fitness"
        else -> "Fitness App Connection"
    }

    fun doLater(): String = when (getLang()) {
        "ko" -> "나중에 하기"
        "ja" -> "後でする"
        "zh" -> "稍后再说"
        "es" -> "Más tarde"
        else -> "Do Later"
    }

    fun foundFitnessApps(): String = when (getLang()) {
        "ko" -> "발견된 피트니스 앱"
        "ja" -> "見つかったフィットネスアプリ"
        "zh" -> "发现的健身应用"
        "es" -> "Apps de fitness encontradas"
        else -> "Found Fitness Apps"
    }

    fun installed(): String = when (getLang()) {
        "ko" -> "설치됨 ✓"
        "ja" -> "インストール済み ✓"
        "zh" -> "已安装 ✓"
        "es" -> "Instalado ✓"
        else -> "Installed ✓"
    }

    fun connecting(): String = when (getLang()) {
        "ko" -> "연결 중..."
        "ja" -> "接続中..."
        "zh" -> "连接中..."
        "es" -> "Conectando..."
        else -> "Connecting..."
    }

    fun connect(): String = when (getLang()) {
        "ko" -> "연결하기"
        "ja" -> "接続する"
        "zh" -> "连接"
        "es" -> "Conectar"
        else -> "Connect"
    }

    fun noFitnessAppFound(): String = when (getLang()) {
        "ko" -> "피트니스 앱을 찾을 수 없습니다\n기본 센서를 사용합니다"
        "ja" -> "フィットネスアプリが見つかりません\n基本センサーを使用します"
        "zh" -> "未找到健身应用\n将使用基本传感器"
        "es" -> "No se encontró app de fitness\nUsando sensor básico"
        else -> "No fitness app found\nUsing basic sensor"
    }

    // ========== AccessibilityConsentStep ==========
    fun speechConsentLoyal(): String = when (getLang()) {
        "ko" -> "앱 차단 권한 설정이야."
        "ja" -> "アプリブロック権限の設定だよ。"
        "zh" -> "这是应用阻止权限设置。"
        "es" -> "Es la configuración de bloqueo."
        else -> "This is the app blocking permission."
    }

    fun speechConsentTsundere(): String = when (getLang()) {
        "ko" -> "권한 설명... 읽어봐."
        "ja" -> "権限の説明... 読んで。"
        "zh" -> "权限说明... 看看吧。"
        "es" -> "Explicación del permiso... léelo."
        else -> "Permission explanation... read it."
    }

    fun speechConsentFoodie(): String = when (getLang()) {
        "ko" -> "권한 동의해주면 차단 기능 쓸 수 있어!"
        "ja" -> "同意してくれたらブロック機能使えるよ！"
        "zh" -> "同意的话就能用阻止功能啦！"
        "es" -> "¡Si aceptas podrás usar el bloqueo!"
        else -> "Accept to use the blocking feature!"
    }

    fun speechConsentPlayful(): String = when (getLang()) {
        "ko" -> "권한 동의해줘!"
        "ja" -> "権限に同意して！"
        "zh" -> "请同意权限！"
        "es" -> "¡Acepta el permiso!"
        else -> "Accept the permission!"
    }

    fun speechConsentTimid(): String = when (getLang()) {
        "ko" -> "권한 동의... 부탁드려요..."
        "ja" -> "権限同意... お願いします..."
        "zh" -> "权限同意... 拜托了..."
        "es" -> "Permiso... por favor..."
        else -> "Permission... please..."
    }

    fun speechConsentClumsy(): String = when (getLang()) {
        "ko" -> "이 권한으로 앱 차단할 수 있어!"
        "ja" -> "この権限でアプリをブロックできる！"
        "zh" -> "有了这个权限就能阻止应用了！"
        "es" -> "¡Con este permiso puedes bloquear apps!"
        else -> "With this permission you can block apps!"
    }

    // ========== PermissionCard ==========
    fun allow(): String = when (getLang()) {
        "ko" -> "허용"
        "ja" -> "許可"
        "zh" -> "允许"
        "es" -> "Permitir"
        else -> "Allow"
    }

    // ========== Widget Mockups ==========
    fun steps(): String = when (getLang()) {
        "ko" -> "걸음"
        "ja" -> "歩"
        "zh" -> "步"
        "es" -> "pasos"
        else -> "steps"
    }

    fun petWidgetGreeting(): String = when (getLang()) {
        "ko" -> "안녕!"
        "ja" -> "やあ!"
        "zh" -> "嗨!"
        "es" -> "¡Hola!"
        else -> "Hi!"
    }

    fun quoteMockupText(): String = when (getLang()) {
        "ko" -> "오늘 하루도\n힘내세요"
        "ja" -> "今日も\n頑張って"
        "zh" -> "今天也\n加油"
        "es" -> "¡Ánimo\nhoy también!"
        else -> "Have a\ngreat day"
    }

    fun fasting(): String = when (getLang()) {
        "ko" -> "단식 중"
        "ja" -> "断食中"
        "zh" -> "断食中"
        "es" -> "Ayunando"
        else -> "Fasting"
    }

    fun thankYouKorean(): String = when (getLang()) {
        "ko" -> "감사합니다"
        "ja" -> "감사합니다"
        "zh" -> "감사합니다"
        "es" -> "감사합니다"
        else -> "Thank you"
    }

    fun tapToSolve(): String = when (getLang()) {
        "ko" -> "탭해서 풀기"
        "ja" -> "タップして解く"
        "zh" -> "点击解决"
        "es" -> "Toca para resolver"
        else -> "Tap to solve"
    }

    // ========== WidgetMockup name matching ==========
    fun widgetNameSteps(): String = when (getLang()) {
        "ko" -> "걸음 수"
        "ja" -> "歩数"
        "zh" -> "步数"
        "es" -> "Pasos"
        else -> "Steps"
    }

    fun widgetNamePet(): String = when (getLang()) {
        "ko" -> "펫"
        "ja" -> "ペット"
        "zh" -> "宠物"
        "es" -> "Mascota"
        else -> "Pet"
    }

    fun widgetNameWeather(): String = when (getLang()) {
        "ko" -> "날씨 예보"
        "ja" -> "天気予報"
        "zh" -> "天气预报"
        "es" -> "Pronóstico"
        else -> "Weather"
    }

    fun widgetNameQuote(): String = when (getLang()) {
        "ko" -> "명언"
        "ja" -> "名言"
        "zh" -> "名言"
        "es" -> "Citas"
        else -> "Quotes"
    }

    fun widgetNameFasting(): String = when (getLang()) {
        "ko" -> "단식 타이머"
        "ja" -> "断食タイマー"
        "zh" -> "断食计时器"
        "es" -> "Timer de ayuno"
        else -> "Fasting Timer"
    }

    fun widgetNameVocab(): String = when (getLang()) {
        "ko" -> "오늘의 단어"
        "ja" -> "今日の単語"
        "zh" -> "今日单词"
        "es" -> "Palabra del día"
        else -> "Daily Word"
    }

    fun widgetNameSudoku(): String = when (getLang()) {
        "ko" -> "스도쿠"
        "ja" -> "数独"
        "zh" -> "数独"
        "es" -> "Sudoku"
        else -> "Sudoku"
    }

    // ========== PaymentScreen ==========
    fun promoFreeLoyalSpeech(): String = when (getLang()) {
        "ko" -> "공짜로 가는 거야. 준비해."
        "ja" -> "無料だよ。準備して。"
        "zh" -> "免费的。准备好。"
        "es" -> "Es gratis. Prepárate."
        else -> "It's free. Get ready."
    }

    fun promoFreeTsundereSpeech(): String = when (getLang()) {
        "ko" -> "뭐, 운 좋네. 공짜래."
        "ja" -> "まあ、ラッキーね。無料だって。"
        "zh" -> "嗯，运气不错。免费的。"
        "es" -> "Bueno, suerte. Es gratis."
        else -> "Well, lucky you. It's free."
    }

    fun promoFreeFoodieSpeech(): String = when (getLang()) {
        "ko" -> "우와 공짜야! 야타~!"
        "ja" -> "わー無料だ！やった~!"
        "zh" -> "哇免费的！耶~!"
        "es" -> "¡Guau, es gratis! ¡Sí~!"
        else -> "Wow it's free! Yay~!"
    }

    fun promoFreePlayfulSpeech(): String = when (getLang()) {
        "ko" -> "공짜라카네! 좋다 아이가!"
        "ja" -> "無料だって！いいね!"
        "zh" -> "免费的！太好了!"
        "es" -> "¡Es gratis! ¡Genial!"
        else -> "It's free! Great!"
    }

    fun promoFreeTimidSpeech(): String = when (getLang()) {
        "ko" -> "무, 무료래요...! 다행이에요..."
        "ja" -> "む、無料だって...！よかった..."
        "zh" -> "是、是免费的...！太好了..."
        "es" -> "Es... ¡es gratis...! Qué alivio..."
        else -> "I-It's free...! What a relief..."
    }

    fun promoFreeClumsySpeech(): String = when (getLang()) {
        "ko" -> "공짜라니! 최고의 시작이야!"
        "ja" -> "無料だなんて！最高のスタートだ!"
        "zh" -> "免费的！最好的开始!"
        "es" -> "¡Es gratis! ¡El mejor comienzo!"
        else -> "It's free! Best start ever!"
    }

    fun paidLoyalSpeech(dailyPrice: String?): String {
        val p = dailyPrice ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 ${p}로\n꿈을 이뤄봐."
            "ja" -> "1日${p}で\n夢を叶えよう。"
            "zh" -> "每天${p}\n实现梦想吧。"
            "es" -> "Por ${p} al día\nlogra tus sueños."
            else -> "For just ${p}/day\nachieve your dreams."
        }
    }

    fun paidTsundereSpeech(dailyPrice: String?): String {
        val p = dailyPrice ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 ${p}면 돼...\n꿈 이뤄볼래?"
            "ja" -> "1日${p}でいいの...\n夢叶えてみる?"
            "zh" -> "每天${p}就行...\n要实现梦想吗?"
            "es" -> "Solo ${p}/día...\n¿Quieres lograr tus sueños?"
            else -> "Just ${p}/day...\nWant to achieve your dreams?"
        }
    }

    fun paidFoodieSpeech(dailyPrice: String?): String {
        val p = dailyPrice ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 ${p}로\n꿈을 이뤄보자~!"
            "ja" -> "1日${p}で\n夢を叶えよう~!"
            "zh" -> "每天${p}\n实现梦想吧~!"
            "es" -> "¡Por ${p}/día\nlogra tus sueños~!"
            else -> "For ${p}/day\nlet's achieve dreams~!"
        }
    }

    fun paidPlayfulSpeech(dailyPrice: String?): String {
        val p = dailyPrice ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 ${p}면\n꿈 이룰 수 있다이~"
            "ja" -> "1日${p}で\n夢叶えられるよ~"
            "zh" -> "每天${p}\n就能实现梦想~"
            "es" -> "¡Con ${p}/día\npuedes lograr sueños~"
            else -> "For ${p}/day\nyou can achieve dreams~"
        }
    }

    fun paidTimidSpeech(dailyPrice: String?): String {
        val p = dailyPrice ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하, 하루 ${p}로...\n꿈을 이뤄봐요...!"
            "ja" -> "い、1日${p}で...\n夢を叶えてね...!"
            "zh" -> "每、每天${p}...\n实现梦想吧...!"
            "es" -> "P-Por ${p}/día...\n¡logra tus sueños...!"
            else -> "F-For ${p}/day...\nachieve your dreams...!"
        }
    }

    fun paidClumsySpeech(dailyPrice: String?): String {
        val p = dailyPrice ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 ${p}로 꿈 이루기!\n완전 좋아!"
            "ja" -> "1日${p}で夢を叶える！\n最高！"
            "zh" -> "每天${p}实现梦想！\n太棒了！"
            "es" -> "¡Logra sueños por ${p}/día!\n¡Genial!"
            else -> "Achieve dreams for ${p}/day!\nAwesome!"
        }
    }

    fun processing(): String = when (getLang()) {
        "ko" -> "결제 중..."
        "ja" -> "決済中..."
        "zh" -> "支付中..."
        "es" -> "Procesando..."
        else -> "Processing..."
    }

    fun startForFree(): String = when (getLang()) {
        "ko" -> "3일 무료 체험 시작"
        "ja" -> "3日間無料お試し"
        "zh" -> "3天免费试用"
        "es" -> "3 días gratis"
        else -> "Start 3-Day Free Trial"
    }

    fun trialSubtitle(isYearly: Boolean, yearlyPriceStr: String?, monthlyPriceStr: String?): String {
        val yearly = yearlyPriceStr ?: "$26.99"
        val monthly = monthlyPriceStr ?: "$2.49"
        return when (getLang()) {
            "ko" -> if (isYearly) "3일 후 연 $yearly" else "3일 후 월 $monthly"
            "ja" -> if (isYearly) "3日後に年額$yearly" else "3日後に月額$monthly"
            "zh" -> if (isYearly) "3天后年费$yearly" else "3天后月费$monthly"
            "es" -> if (isYearly) "Después $yearly/año" else "Después $monthly/mes"
            else -> if (isYearly) "Then $yearly/year" else "Then $monthly/month"
        }
    }

    fun restartAgain(): String = when (getLang()) {
        "ko" -> "다시 시작하기"
        "ja" -> "もう一度始める"
        "zh" -> "重新开始"
        "es" -> "Empezar de nuevo"
        else -> "Start Again"
    }

    fun activityNotFound(): String = when (getLang()) {
        "ko" -> "Activity를 찾을 수 없습니다"
        "ja" -> "Activityが見つかりません"
        "zh" -> "找不到Activity"
        "es" -> "No se encontró la Activity"
        else -> "Activity not found"
    }

    fun subscriptionSaveFailed(): String = when (getLang()) {
        "ko" -> "구독 정보 저장 실패"
        "ja" -> "サブスクリプション情報の保存に失敗"
        "zh" -> "保存订阅信息失败"
        "es" -> "Error al guardar suscripción"
        else -> "Failed to save subscription"
    }

    fun errorPrefix(): String = when (getLang()) {
        "ko" -> "오류:"
        "ja" -> "エラー:"
        "zh" -> "错误:"
        "es" -> "Error:"
        else -> "Error:"
    }

    fun monthAfterChanged(): String = when (getLang()) {
        "ko" -> "한 달 뒤, 달라진 나"
        "ja" -> "1ヶ月後、変わった私"
        "zh" -> "一个月后，改变的我"
        "es" -> "Un mes después, un nuevo yo"
        else -> "A month later, a changed me"
    }

    fun freeStart(): String = when (getLang()) {
        "ko" -> "무료로 시작!"
        "ja" -> "無料でスタート!"
        "zh" -> "免费开始!"
        "es" -> "¡Empieza gratis!"
        else -> "Start for Free!"
    }

    fun oneMonthAllFeaturesFree(): String = when (getLang()) {
        "ko" -> "1달간 모든 기능 무료!"
        "ja" -> "1ヶ月間すべての機能が無料!"
        "zh" -> "一个月所有功能免费!"
        "es" -> "¡Todas las funciones gratis por 1 mes!"
        else -> "All features free for 1 month!"
    }

    fun canInviteOneFriendFree(): String = when (getLang()) {
        "ko" -> "친구 1명도 무료 초대 가능!"
        "ja" -> "友達1人も無料招待可能!"
        "zh" -> "还可以免费邀请1位朋友!"
        "es" -> "¡También puedes invitar 1 amigo gratis!"
        else -> "You can also invite 1 friend for free!"
    }

    fun perYear(): String = when (getLang()) {
        "ko" -> "/년"
        "ja" -> "/年"
        "zh" -> "/年"
        "es" -> "won/año"
        else -> "/year"
    }

    fun perMonth(): String = when (getLang()) {
        "ko" -> "/월"
        "ja" -> "/月"
        "zh" -> "/月"
        "es" -> "won/mes"
        else -> "/month"
    }

    fun dailyPriceCents(dailyPriceStr: String?): String {
        val price = dailyPriceStr ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 약 $price"
            "ja" -> "1日約$price"
            "zh" -> "每天约$price"
            "es" -> "$price/día"
            else -> "$price/day"
        }
    }

    // 하루 약 N원으로 인생을 바꿔보세요 슬로건
    fun changYourLifeSlogan(dailyPriceStr: String?): String {
        val price = dailyPriceStr ?: "~8¢"
        return when (getLang()) {
            "ko" -> "하루 $price 로 인생을 바꿔보세요"
            "ja" -> "1日${price}で人生を変えよう"
            "zh" -> "每天${price}改变你的人生"
            "es" -> "Cambia tu vida por $price al día"
            else -> "Change your life for $price a day"
        }
    }

    fun benefitAiPetCare(): String = when (getLang()) {
        "ko" -> "AI 펫 케어"
        "ja" -> "AIペットケア"
        "zh" -> "AI宠物照顾"
        "es" -> "Cuidado de mascota IA"
        else -> "AI Pet Care"
    }

    fun benefitAiPetCareDesc(): String = when (getLang()) {
        "ko" -> "매일 대화하며 함께 성장해요"
        "ja" -> "毎日会話しながら一緒に成長"
        "zh" -> "每天对话，共同成长"
        "es" -> "Crece juntos conversando cada día"
        else -> "Grow together with daily chats"
    }

    fun benefitSmartBlock(): String = when (getLang()) {
        "ko" -> "스마트 앱 차단"
        "ja" -> "スマートアプリブロック"
        "zh" -> "智能应用屏蔽"
        "es" -> "Bloqueo inteligente de apps"
        else -> "Smart App Blocking"
    }

    fun benefitSmartBlockDesc(): String = when (getLang()) {
        "ko" -> "목표 달성 전까지 유혹 차단"
        "ja" -> "目標達成まで誘惑をブロック"
        "zh" -> "在达成目标前屏蔽诱惑"
        "es" -> "Bloquea tentaciones hasta lograr metas"
        else -> "Block temptations until goal achieved"
    }

    fun benefitHomeWidget(): String = when (getLang()) {
        "ko" -> "홈 위젯"
        "ja" -> "ホームウィジェット"
        "zh" -> "主屏幕小组件"
        "es" -> "Widget de inicio"
        else -> "Home Widget"
    }

    fun benefitHomeWidgetDesc(): String = when (getLang()) {
        "ko" -> "홈 화면에서 바로 확인"
        "ja" -> "ホーム画面ですぐ確認"
        "zh" -> "在主屏幕直接查看"
        "es" -> "Verifica desde la pantalla de inicio"
        else -> "Check directly from home screen"
    }

    fun benefitInvite12Friends(): String = when (getLang()) {
        "ko" -> "친구 12명 초대"
        "ja" -> "友達12人招待"
        "zh" -> "邀请12位朋友"
        "es" -> "Invitar 12 amigos"
        else -> "Invite 12 friends"
    }

    fun benefitInvite1Friend(): String = when (getLang()) {
        "ko" -> "친구 1명 초대"
        "ja" -> "友達1人招待"
        "zh" -> "邀请1位朋友"
        "es" -> "Invitar 1 amigo"
        else -> "Invite 1 friend"
    }

    fun benefitInviteDesc(): String = when (getLang()) {
        "ko" -> "친구도 무료로 시작 가능"
        "ja" -> "友達も無料でスタート可能"
        "zh" -> "朋友也可以免费开始"
        "es" -> "Amigos también empiezan gratis"
        else -> "Friends can also start for free"
    }

    fun socialProof(): String = when (getLang()) {
        "ko" -> "1,000+ 사용자와 함께하고 있어요"
        "ja" -> "1,000人以上のユーザーと一緒に"
        "zh" -> "已有1,000+用户加入"
        "es" -> "Únete a más de 1,000 usuarios"
        else -> "Join 1,000+ users"
    }

    fun applied(): String = when (getLang()) {
        "ko" -> "적용 완료"
        "ja" -> "適用完了"
        "zh" -> "已应用"
        "es" -> "Aplicado"
        else -> "Applied"
    }

    fun inviteCode(): String = when (getLang()) {
        "ko" -> "초대 코드"
        "ja" -> "招待コード"
        "zh" -> "邀请码"
        "es" -> "Código de invitación"
        else -> "Invite Code"
    }

    fun enterCode(): String = when (getLang()) {
        "ko" -> "코드 입력"
        "ja" -> "コード入力"
        "zh" -> "输入代码"
        "es" -> "Ingresa código"
        else -> "Enter code"
    }

    fun verifying(): String = when (getLang()) {
        "ko" -> "확인 중..."
        "ja" -> "確認中..."
        "zh" -> "验证中..."
        "es" -> "Verificando..."
        else -> "Verifying..."
    }

    fun apply(): String = when (getLang()) {
        "ko" -> "적용"
        "ja" -> "適用"
        "zh" -> "应用"
        "es" -> "Aplicar"
        else -> "Apply"
    }

    // ========== getPetDescription (V1 Legacy) ==========
    fun descDog1(): String = when (getLang()) {
        "ko" -> "듬직하고 멋있는 상남자 스타일\n말수는 적지만 행동으로 보여주는 타입\n묵묵히 당신 곁을 지켜줄 거예요"
        "ja" -> "頼もしくてかっこいい男らしいスタイル\n口数は少ないけど行動で示すタイプ\n黙々とあなたのそばを守ります"
        "zh" -> "稳重帅气的硬汉风格\n话不多但用行动证明\n会默默守护在你身边"
        "es" -> "Estilo varonil y confiable\nPocas palabras, muchas acciones\nTe protegerá en silencio"
        else -> "Reliable and cool manly style\nFew words but shows through actions\nWill silently protect you"
    }

    fun descDog2(): String = when (getLang()) {
        "ko" -> "갓생러 지망 강아지\nㄹㅇ 응원이 특기ㅋㅋ\n같이 있으면 텐션 업 보장"
        "ja" -> "意識高い系を目指す犬\nマジで応援が特技ww\n一緒にいるとテンションアップ保証"
        "zh" -> "立志成为优秀狗狗\n超会加油打气\n跟它在一起保证心情好"
        "es" -> "Perrito aspirante a mejor vida\nExperto en animarte jaja\nTe sube el ánimo garantizado"
        else -> "Aspiring best-life puppy\nCheering is their specialty lol\nGuaranteed mood boost"
    }

    fun descCat1(): String = when (getLang()) {
        "ko" -> "겉은 차갑지만 속은 따뜻한 츤데레\n관심 없는 척하지만 사실 다 챙겨요\n은근히 당신 걱정을 많이 해요"
        "ja" -> "外は冷たいけど中は温かいツンデレ\n興味ないふりしてるけど実は全部気にしてる\nこっそりあなたのこと心配してます"
        "zh" -> "外冷内热的傲娇\n装作不在乎其实都记着\n其实很担心你"
        "es" -> "Tsundere frío por fuera, cálido por dentro\nFinge no importarle pero te cuida\nSecretamente se preocupa mucho"
        else -> "Cold outside, warm inside tsundere\nPretends not to care but watches over you\nSecretly worries about you a lot"
    }

    fun descCat2(): String = when (getLang()) {
        "ko" -> "쿨한 부산 고양이\n담백하고 솔직한 말투가 매력\n옆에서 든든하게 챙겨줄 거예요"
        "ja" -> "クールな釜山の猫\nあっさり正直な話し方が魅力\nそばでしっかり面倒見てくれます"
        "zh" -> "酷酷的釜山猫\n简洁直率的说话方式很有魅力\n会在旁边可靠地照顾你"
        "es" -> "Gato cool de Busan\nHabla directo y sincero\nTe cuidará con firmeza"
        else -> "Cool Busan cat\nDirect and honest way of speaking\nWill reliably take care of you"
    }

    fun descRat(): String = when (getLang()) {
        "ko" -> "소심하지만 마음은 따뜻해요\n조심스럽게 당신에게 다가가요\n천천히 친해지면 든든한 친구가 돼요"
        "ja" -> "臆病だけど心は温かい\n慎重にあなたに近づきます\nゆっくり仲良くなれば頼もしい友達に"
        "zh" -> "虽然胆小但内心温暖\n小心翼翼地接近你\n慢慢熟悉后会成为可靠的朋友"
        "es" -> "Tímido pero de corazón cálido\nSe acerca con cuidado\nSe vuelve un amigo confiable poco a poco"
        else -> "Shy but warm-hearted\nApproaches you carefully\nBecomes a reliable friend slowly"
    }

    fun descBird(): String = when (getLang()) {
        "ko" -> "언제나 밝고 긍정적인 에너지\n힘들 때 용기를 북돋아 줘요\n함께라면 매일이 즐거워요"
        "ja" -> "いつも明るくポジティブなエネルギー\n辛い時に勇気をくれます\n一緒なら毎日が楽しい"
        "zh" -> "永远阳光积极的能量\n困难时给你勇气\n在一起每天都很开心"
        "es" -> "Siempre brillante y positivo\nTe anima cuando estás mal\nCada día es divertido juntos"
        else -> "Always bright and positive energy\nEncourages you when times are hard\nEvery day is fun together"
    }
}

/**
 * 접근성 권한 동의 다국어 문자열 (Google Play 정책 준수)
 *
 * Google Play 정책 요구사항:
 * 1. 권한 요청 전 명시적 공개 대화상자 제시
 * 2. 사용자의 확실한 동의 표현 요구 (체크박스)
 * 3. 대화상자 나가기를 동의로 해석 금지
 * 4. 두 개의 버튼 필수 (동의/거부)
 * 5. Google 권장 형식: "[앱]은 [기능]을 위해 [데이터]를 수집합니다"
 */
private object AccessibilityConsentStrings {
    private fun getLang(): String = java.util.Locale.getDefault().language

    // ===== 제목 =====
    fun title(): String = when (getLang()) {
        "ko" -> "AccessibilityService API 사용에 대한 명시적 공개"
        "ja" -> "AccessibilityService API使用に関する明示的開示"
        "zh" -> "关于AccessibilityService API使用的明确披露"
        "es" -> "Divulgación Explícita sobre el uso de AccessibilityService API"
        else -> "Explicit Disclosure for AccessibilityService API Usage"
    }

    // ===== Google 권장 형식 공개 문구 =====
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
        "es" -> "rebon recopila el nombre del paquete de la app en ejecución " +
                "(evento TYPE_WINDOW_STATE_CHANGED) para habilitar la función de bloqueo de apps. " +
                "Estos datos se procesan solo en el dispositivo y no se transmiten a servidores externos."
        else -> "rebon collects the package name of the currently running app " +
                "(TYPE_WINDOW_STATE_CHANGED event) to enable the app blocking feature. " +
                "This data is processed only on your device and is not transmitted to external servers."
    }

    // ===== 섹션 제목들 =====
    fun sectionDataCollected(): String = when (getLang()) {
        "ko" -> "1. 수집하는 데이터"
        "ja" -> "1. 収集するデータ"
        "zh" -> "1. 收集的数据"
        "es" -> "1. Datos recopilados"
        else -> "1. Data Collected"
    }

    fun sectionDataUsage(): String = when (getLang()) {
        "ko" -> "2. 데이터 사용 방법"
        "ja" -> "2. データの使用方法"
        "zh" -> "2. 数据使用方式"
        "es" -> "2. Cómo se usan los datos"
        else -> "2. How Data is Used"
    }

    fun sectionDataNotCollected(): String = when (getLang()) {
        "ko" -> "3. 수집하지 않는 데이터"
        "ja" -> "3. 収集しないデータ"
        "zh" -> "3. 不收集的数据"
        "es" -> "3. Datos NO recopilados"
        else -> "3. Data NOT Collected"
    }

    fun sectionWithdrawal(): String = when (getLang()) {
        "ko" -> "4. 동의 철회 방법"
        "ja" -> "4. 同意撤回方法"
        "zh" -> "4. 撤回同意的方法"
        "es" -> "4. Cómo revocar el consentimiento"
        else -> "4. How to Withdraw Consent"
    }

    // ===== 섹션 내용들 =====
    fun dataCollectedContent(): String = when (getLang()) {
        "ko" -> "• 현재 화면에 표시된 앱의 패키지명\n" +
                "• TYPE_WINDOW_STATE_CHANGED 이벤트 정보"
        "ja" -> "• 現在表示中のアプリのパッケージ名\n" +
                "• TYPE_WINDOW_STATE_CHANGEDイベント情報"
        "zh" -> "• 当前显示应用的包名\n" +
                "• TYPE_WINDOW_STATE_CHANGED事件信息"
        "es" -> "• Nombre del paquete de la app actual\n" +
                "• Información del evento TYPE_WINDOW_STATE_CHANGED"
        else -> "• Package name of currently displayed app\n" +
                "• TYPE_WINDOW_STATE_CHANGED event information"
    }

    fun dataUsageContent(): String = when (getLang()) {
        "ko" -> "• 차단 설정된 앱 실행 감지 시 홈 화면으로 이동\n" +
                "• 모든 데이터는 기기 내에서만 처리됨\n" +
                "• 외부 서버 전송 없음\n" +
                "• 제3자 공유 없음"
        "ja" -> "• ブロック設定アプリ検出時にホーム画面へ移動\n" +
                "• すべてのデータはデバイス内でのみ処理\n" +
                "• 外部サーバーへの送信なし\n" +
                "• 第三者との共有なし"
        "zh" -> "• 检测到被阻止的应用时返回主屏幕\n" +
                "• 所有数据仅在设备内处理\n" +
                "• 不传输到外部服务器\n" +
                "• 不与第三方共享"
        "es" -> "• Ir a inicio cuando se detecta app bloqueada\n" +
                "• Todos los datos se procesan solo en el dispositivo\n" +
                "• Sin transmisión a servidores externos\n" +
                "• Sin compartir con terceros"
        else -> "• Navigate to home when blocked app is detected\n" +
                "• All data processed only on device\n" +
                "• No transmission to external servers\n" +
                "• No sharing with third parties"
    }

    fun dataNotCollectedContent(): String = when (getLang()) {
        "ko" -> "• 화면 내용 또는 텍스트\n" +
                "• 입력한 텍스트 또는 비밀번호\n" +
                "• 앱 사용 기록 또는 브라우징 기록\n" +
                "• 개인 식별 정보"
        "ja" -> "• 画面内容またはテキスト\n" +
                "• 入力テキストまたはパスワード\n" +
                "• アプリ使用履歴またはブラウジング履歴\n" +
                "• 個人識別情報"
        "zh" -> "• 屏幕内容或文字\n" +
                "• 输入的文字或密码\n" +
                "• 应用使用记录或浏览记录\n" +
                "• 个人身份信息"
        "es" -> "• Contenido de pantalla o texto\n" +
                "• Texto ingresado o contraseñas\n" +
                "• Historial de uso de apps o navegación\n" +
                "• Información de identificación personal"
        else -> "• Screen content or text\n" +
                "• Typed text or passwords\n" +
                "• App usage or browsing history\n" +
                "• Personal identification information"
    }

    fun withdrawalContent(): String = when (getLang()) {
        "ko" -> "설정 > 접근성 > 설치된 앱 > rebon > 사용 안 함"
        "ja" -> "設定 > ユーザー補助 > インストール済みアプリ > rebon > オフ"
        "zh" -> "设置 > 无障碍 > 已安装的应用 > rebon > 关闭"
        "es" -> "Ajustes > Accesibilidad > Apps instaladas > rebon > Desactivar"
        else -> "Settings > Accessibility > Installed apps > rebon > Turn off"
    }

    // ===== 4개 체크박스 라벨 (Google Play 정책: 명시적 동의) =====
    fun checkbox1DataCollection(): String = when (getLang()) {
        "ko" -> "위 '수집하는 데이터' 내용을 읽고 이해했습니다."
        "ja" -> "上記の「収集するデータ」の内容を読んで理解しました。"
        "zh" -> "我已阅读并理解上述'收集的数据'内容。"
        "es" -> "He leído y comprendido el contenido de 'Datos recopilados' anterior."
        else -> "I have read and understood the 'Data Collected' content above."
    }

    fun checkbox2DataUsage(): String = when (getLang()) {
        "ko" -> "위 '데이터 사용 방법' 내용을 읽고 이해했습니다."
        "ja" -> "上記の「データの使用方法」の内容を読んで理解しました。"
        "zh" -> "我已阅读并理解上述'数据使用方式'内容。"
        "es" -> "He leído y comprendido el contenido de 'Cómo se usan los datos' anterior."
        else -> "I have read and understood the 'How Data is Used' content above."
    }

    fun checkbox3Withdrawal(): String = when (getLang()) {
        "ko" -> "위 '동의 철회 방법'을 읽고 이해했습니다."
        "ja" -> "上記の「同意撤回方法」を読んで理解しました。"
        "zh" -> "我已阅读并理解上述'撤回同意的方法'内容。"
        "es" -> "He leído y comprendido 'Cómo revocar el consentimiento' anterior."
        else -> "I have read and understood 'How to Withdraw Consent' above."
    }

    fun checkbox4FinalConsent(): String = when (getLang()) {
        "ko" -> "AccessibilityService API 사용에 동의합니다."
        "ja" -> "AccessibilityService APIの使用に同意します。"
        "zh" -> "我同意使用AccessibilityService API。"
        "es" -> "Acepto el uso de AccessibilityService API."
        else -> "I agree to the use of AccessibilityService API."
    }

    // ===== 버튼 (Google Play 정책: 두 개 버튼 필수, 명시적 동의 표현) =====
    fun agreeButton(): String = when (getLang()) {
        "ko" -> "동의하고 설정으로 이동"
        "ja" -> "同意して設定へ移動"
        "zh" -> "同意并前往设置"
        "es" -> "Acepto e ir a Ajustes"
        else -> "I Agree & Go to Settings"
    }

    fun declineButton(): String = when (getLang()) {
        "ko" -> "동의하지 않습니다"
        "ja" -> "同意しません"
        "zh" -> "我不同意"
        "es" -> "No acepto"
        else -> "I Decline"
    }

    // ===== 거부 다이얼로그 =====
    fun declinedTitle(): String = when (getLang()) {
        "ko" -> "앱을 사용할 수 없습니다"
        "ja" -> "アプリを使用できません"
        "zh" -> "无法使用应用"
        "es" -> "No se puede usar la app"
        else -> "Cannot Use App"
    }

    fun declinedMessage(): String = when (getLang()) {
        "ko" -> "AccessibilityService API 권한은 rebon의 핵심 앱 차단 기능에 필수입니다.\n\n" +
                "Android에서 현재 실행 중인 앱을 감지할 수 있는 유일한 방법이기 때문입니다.\n\n" +
                "이 권한 없이는 앱 차단 기능이 작동하지 않습니다."
        "ja" -> "AccessibilityService API権限は、rebonのアプリブロック機能に必須です。\n\n" +
                "Androidで実行中のアプリを検出する唯一の方法だからです。\n\n" +
                "この権限なしではアプリブロック機能が動作しません。"
        "zh" -> "AccessibilityService API权限是rebon核心应用阻止功能的必需条件。\n\n" +
                "因为这是Android中检测当前运行应用的唯一方法。\n\n" +
                "没有此权限，应用阻止功能将无法工作。"
        "es" -> "El permiso de AccessibilityService API es esencial para la función de bloqueo de apps de rebon.\n\n" +
                "Es el único método en Android para detectar la app en ejecución.\n\n" +
                "Sin este permiso, la función de bloqueo no funcionará."
        else -> "AccessibilityService API permission is essential for rebon's core app blocking feature.\n\n" +
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

    // ===== 설정 안내 (동의 후 표시) =====
    fun settingsInstructions(): String = when (getLang()) {
        "ko" -> "설정 방법:\n" +
                "1. 'rebon'을 찾아 탭하세요\n" +
                "2. 'rebon 사용' 스위치를 켜세요\n" +
                "3. '허용' 버튼을 탭하세요"
        "ja" -> "設定方法:\n" +
                "1. 'rebon'を見つけてタップ\n" +
                "2. 'rebonを使用'スイッチをオン\n" +
                "3. '許可'ボタンをタップ"
        "zh" -> "设置方法:\n" +
                "1. 找到并点击'rebon'\n" +
                "2. 打开'使用rebon'开关\n" +
                "3. 点击'允许'按钮"
        "es" -> "Cómo configurar:\n" +
                "1. Encuentra y toca 'rebon'\n" +
                "2. Activa el interruptor 'Usar rebon'\n" +
                "3. Toca el botón 'Permitir'"
        else -> "How to set up:\n" +
                "1. Find and tap 'rebon'\n" +
                "2. Turn on 'Use rebon' switch\n" +
                "3. Tap 'Allow' button"
    }

    // 상세 보기 버튼
    fun viewDetails(): String = when (getLang()) {
        "ko" -> "상세 내용 보기"
        "ja" -> "詳細を見る"
        "zh" -> "查看详情"
        "es" -> "Ver detalles"
        else -> "View Details"
    }

    // 펫 말풍선용 메시지
    fun petSpeech(): String = when (getLang()) {
        "ko" -> "앱 차단 기능을 위해\n접근성 권한이 필요해요!"
        "ja" -> "アプリブロック機能のため\nアクセシビリティ権限が必要です!"
        "zh" -> "需要无障碍权限\n来启用应用阻止功能!"
        "es" -> "Se necesita permiso de\naccesibilidad para bloquear apps!"
        else -> "Accessibility permission needed\nfor app blocking feature!"
    }

    // 간단 안내 문구
    fun shortDescription(): String = when (getLang()) {
        "ko" -> "rebon은 앱 차단을 위해 현재 실행 중인 앱을 감지합니다.\n모든 데이터는 기기 내에서만 처리됩니다."
        "ja" -> "rebonはアプリブロックのため実行中のアプリを検出します。\nすべてのデータはデバイス内でのみ処理されます。"
        "zh" -> "rebon会检测当前运行的应用以进行阻止。\n所有数据仅在设备内处理。"
        "es" -> "rebon detecta la app en ejecucion para bloquearla.\nTodos los datos se procesan solo en el dispositivo."
        else -> "rebon detects the running app for blocking.\nAll data is processed only on your device."
    }

    // 체크박스 통합 라벨
    fun consentCheckboxLabel(): String = when (getLang()) {
        "ko" -> "위 내용을 확인했으며, AccessibilityService API 사용에 동의합니다."
        "ja" -> "上記の内容を確認し、AccessibilityService APIの使用に同意します。"
        "zh" -> "我已确认上述内容，同意使用AccessibilityService API。"
        "es" -> "He revisado lo anterior y acepto el uso de AccessibilityService API."
        else -> "I have reviewed the above and agree to use AccessibilityService API."
    }
}

/**
 * Complete Pet Onboarding Flow - 17 Steps:
 *
 * NO DOTS (0-3):
 * 0. Google Sign-In (데이터 백업) - 기존 데이터 있으면 메인으로 스킵
 * 1. Pet Selection
 * 2. Pet Name Input
 * 3. Tutorial All-in-One (함께 할 것 설명)
 *
 * WITH DOTS (4-15, 10 dots total):
 * 4. Permission Settings (권한 설정) - dot 0
 * 5. Fitness App Connection (피트니스 연결) - dot 1
 * 6. Accessibility (접근성 권한) - dot 2
 * 7. App Selection (앱 선택) - dot 3
 * 8. Test Blocking (차단 테스트) - dot 4
 * 9. Goal Input (목표 입력) - dot 5
 * 10. Walking Test (걷기 테스트) - dot 6
 * 11. How It Works (사용법 설명 - 11+12 통합) - dot 7
 * [12-14 SKIPPED: 기본값 사용 (월~금, 전체 시간대)]
 * 16. Widget Setup (위젯 설정) - dot 8
 * 15. Payment (결제) - dot 9 (마지막)
 *
 * NO DOTS (16):
 * 16. Widget Setup (위젯 설정)
 */
@Composable
fun PetOnboardingScreen(
    onComplete: (PetTypeV2, String) -> Unit,
    onDataRestored: () -> Unit = {},  // 기존 데이터 복원 시 튜토리얼 스킵
    hapticManager: HapticManager? = null,
    preferenceManager: PreferenceManager? = null
) {
    val context = LocalContext.current
    val prefManager = preferenceManager ?: remember { PreferenceManager(context) }

    // 저장된 펫 정보 불러오기 (V2)
    val savedPetTypeName = remember { prefManager.getPetTypeV2()?.name }
    val savedPetName = remember { prefManager.getPetNameV2() }
    val savedPetType = remember {
        if (savedPetTypeName != null) PetTypeV2.entries.find { it.name == savedPetTypeName } else null
    }

    // 저장된 단계 불러오기 (펫 정보가 있어야만 복원)
    val savedStep = remember {
        val step = prefManager.getTutorialCurrentStep()
        // 펫 정보가 필요한 단계(4 이상)인데 펫 정보가 없으면 0으로 리셋
        // Step 0: Google Sign-In, Step 1-2: Pet setup, Step 3: Tutorial + Google login, Step 4+: Main tutorial
        if (step >= 4 && savedPetType == null) 0 else step
    }

    var currentStep by rememberSaveable { mutableIntStateOf(savedStep) }
    // PreferenceManager와 동기화 (recomposition 시 항상 최신 값 사용)
    val currentSavedPetType = prefManager.getPetTypeV2()
    var selectedPetType by remember(currentSavedPetType) { mutableStateOf(currentSavedPetType) }
    var petName by remember { mutableStateOf(if (savedStep > 1 && savedPetName.isNotBlank()) savedPetName else "") }

    // 단계 변경 시 저장 및 Analytics 추적
    LaunchedEffect(currentStep) {
        prefManager.saveTutorialCurrentStep(currentStep)

        // Analytics: 튜토리얼 단계 추적
        if (currentStep == 0) {
            AnalyticsManager.trackTutorialBegin()
        }
        AnalyticsManager.trackTutorialStep(currentStep)
    }

    // Analytics: 튜토리얼 이탈 추적 (앱 종료 또는 화면 이탈 시)
    DisposableEffect(Unit) {
        onDispose {
            // 튜토리얼 완료 전에 이탈한 경우 추적
            if (currentStep < 16) {
                AnalyticsManager.trackTutorialExit(currentStep)
            }
        }
    }

    // 네비게이션 닷 계산 (Step 4-16는 닷 표시, 10개) - Step 12-14 스킵, 위젯(16)→결제(15) 순서
    val showDots = currentStep in 4..16
    val dotStep = if (showDots) {
        when {
            currentStep == 16 -> 8  // 위젯 (dot 8)
            currentStep == 15 -> 9  // 결제 (dot 9, 마지막)
            else -> currentStep - 4
        }
    } else 0
    val totalDots = 10

    // null-safe 로컬 변수 캡처
    val currentSelectedPetType = selectedPetType

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        when (currentStep) {
            // === NO DOTS (0) - Google 로그인 (필수) ===
            0 -> GoogleSignInStep(
                hapticManager = hapticManager,
                onNext = {
                    // 신규 사용자: 펫 선택으로
                    hapticManager?.click()
                    currentStep = 1
                },
                onDataRestored = {
                    // 기존 사용자: 튜토리얼 스킵하고 메인으로
                    hapticManager?.success()
                    onDataRestored()
                }
            )

            // === NO DOTS (1-3) ===
            1 -> PetSelectionStep(
                selectedPet = selectedPetType,
                onPetSelected = {
                    selectedPetType = it
                    // 펫 선택 시 바로 저장 (V2)
                    prefManager.savePetTypeV2(it)
                    // 위젯 업데이트
                    StepWidgetProvider.updateAllWidgets(context)
                    // Analytics: 펫 선택 추적
                    AnalyticsManager.trackPetSelected(it.name)
                },
                onNext = {
                    hapticManager?.click()
                    currentStep = 2
                },
                hapticManager = hapticManager
            )
            2 -> if (currentSelectedPetType != null) PetNameInputStep(
                petType = currentSelectedPetType,
                currentName = petName,
                onNameChanged = {
                    petName = it
                    // 이름 입력 시 바로 저장 (V2)
                    prefManager.savePetNameV2(it)
                },
                onNext = {
                    hapticManager?.click()
                    currentStep = 3  // 튜토리얼 + 구글 로그인으로
                },
                hapticManager = hapticManager
            )

            // === NO DOTS (3) - 튜토리얼 안내 (Google 로그인은 step 0에서 완료) ===
            3 -> if (currentSelectedPetType != null) TutorialAllInOneStep(
                petType = currentSelectedPetType,
                petName = petName,
                hapticManager = hapticManager,
                onNext = {
                    currentStep = 4
                }
            )

            // === WITH DOTS (4-15) ===
            4 -> if (currentSelectedPetType != null) PermissionSettingsStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 5  // AccessibilityConsentStep로 이동
                }
            )
            // Step 5: 접근성 권한 동의 + 설정 (Google Play 정책 준수)
            // - 4개 체크박스로 명시적 동의
            // - 동의 후 바로 시스템 접근성 설정으로 이동
            // - 접근성 활성화 확인 후 자동으로 다음 단계로
            5 -> if (currentSelectedPetType != null) AccessibilityConsentStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onAgree = {
                    hapticManager?.click()
                    currentStep = 7  // 접근성 활성화 확인 후 앱 선택으로 바로 이동
                }
            )
            // Step 6: 제거됨 (Step 5에서 동의 + 설정 + 확인 모두 처리)
            7 -> if (currentSelectedPetType != null) AppSelectionStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 8
                }
            )
            8 -> if (currentSelectedPetType != null) TestBlockingStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    // 차단 테스트 상태 클리어
                    prefManager.clearBlockingTestStarted()
                    currentStep = 9
                }
            )
            9 -> if (currentSelectedPetType != null) GoalInputStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 10
                }
            )
            10 -> if (currentSelectedPetType != null) WalkingTestStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 11
                }
            )
            11 -> if (currentSelectedPetType != null) HowItWorksStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    // 기본값으로 저장하고 12-14 스킵, 위젯(16)으로 이동
                    prefManager.saveControlDays(setOf(1, 2, 3, 4, 5))  // 월~금
                    prefManager.saveBlockingPeriods(setOf("morning", "afternoon", "evening", "night"))  // 전체 시간
                    currentStep = 16
                }
            )
            // Step 12-14 스킵됨
            13 -> if (currentSelectedPetType != null) ControlDaysStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 14
                }
            )
            14 -> if (currentSelectedPetType != null) BlockTimeStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 15
                }
            )
            // === 위젯 먼저, 결제 나중 ===
            16 -> if (currentSelectedPetType != null) WidgetSetupStep(
                petType = currentSelectedPetType,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onComplete = {
                    hapticManager?.click()
                    currentStep = 15  // 결제로 이동
                }
            )

            15 -> if (currentSelectedPetType != null) PaymentScreen(
                petType = currentSelectedPetType,
                petName = petName,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onComplete = {
                    hapticManager?.success()
                    // 모든 튜토리얼 단계 완료 플래그 설정
                    prefManager.setPermissionSetupCompleted(true)
                    prefManager.setHealthConnectSetupCompleted(true)
                    prefManager.setAccessibilitySetupCompleted(true)
                    prefManager.setAppSelectionCompleted(true)
                    prefManager.setTutorialCompleted(true)
                    // 센서 초기 걸음수 리셋 (메인화면에서 깨끗하게 시작)
                    prefManager.saveInitialSteps(-1)
                    // 신규 유저 방어권 1개 지급
                    prefManager.addStreakDefenseTickets(1)
                    // paidDeposit은 saveTutorialCompletionData에서 프로모션 여부 확인 후 설정
                    // 튜토리얼 진행 단계 초기화
                    prefManager.clearTutorialCurrentStep()
                    // 실제 목표 설정 필요 플래그
                    prefManager.setNeedsRealGoalSetup(true)

                    // Firebase에 모든 데이터 한 번에 동기화 (앱 재설치 시 복원용)
                    val app = context.applicationContext as WalkorWaitApp
                    val repo = app.userDataRepository
                    repo.saveTutorialCompletionData(
                        lockedApps = prefManager.getLockedApps(),
                        blockingPeriods = prefManager.getBlockingPeriods(),
                        controlDays = prefManager.getControlDays(),
                        goal = prefManager.getGoal(),
                        deposit = prefManager.getDeposit(),
                        controlStartDate = prefManager.getControlStartDate(),
                        controlEndDate = prefManager.getControlEndDate(),
                        petType = currentSelectedPetType.name,
                        petName = petName
                    )

                    // Analytics: 튜토리얼 완료 추적
                    AnalyticsManager.trackTutorialComplete()
                    AnalyticsManager.setUserPetType(currentSelectedPetType.name)

                    onComplete(currentSelectedPetType, petName)
                }
            )
        }
    }
}

/**
 * Step 1: Pet Selection - basic.png 목업 + Game Boy LCD 스타일 (V2 펫 사용)
 */
@Composable
private fun PetSelectionStep(
    selectedPet: PetTypeV2?,
    onPetSelected: (PetTypeV2) -> Unit,
    onNext: () -> Unit,
    hapticManager: HapticManager?
) {
    val kenneyFont = rememberKenneyFont()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 72.dp),  // 네비게이션 바 고려하여 증가
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - Kenney Font
        Text(
            text = "rebon",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Pet display area - 스트라이프 배경 + 둥근 모서리
        // 모든 펫 동일 크기로 표시 (목업 기준)
        val displayPetSize = 140.dp // 디스플레이 영역 내 펫 크기 고정
        val displayShadowWidth = 100.dp
        val stripeWidth = 4.dp // 픽셀 아트에 맞는 스트라이프 너비

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = stripeWidth.toPx()
                    val stripeColor = Color(0xFFF0F0F0) // 연한 그레이
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = stripeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, y),
                            size = androidx.compose.ui.geometry.Size(size.width, stripeHeightPx)
                        )
                        y += stripeHeightPx * 2
                    }
                }
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedPet != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Speech bubble (V2 성격 사용)
                    val greeting = PetDialoguesV2.getWelcomeMessage(selectedPet.personality, "")
                    SpeechBubble(
                        text = greeting,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Pet sprite with glow (V2 - BABY 단계 표시)
                    PetSpriteV2WithGlow(
                        petType = selectedPet,
                        stage = PetGrowthStage.BABY,
                        animationType = PetAnimationTypeV2.IDLE,
                        size = displayPetSize,
                        monochrome = true,
                        showGlow = true,
                        applyDisplayScale = false  // 선택화면에서는 원본 크기 유지
                    )
                }
            } else {
                Text(
                    text = "?",
                    fontSize = 80.sp,
                    fontFamily = kenneyFont,
                    color = Color(0xFF555555).copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.choose_your_friend),
            fontSize = 22.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Pet selection grid - 3x2
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1 (SHIBA, CAT, PIG)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PetTypeV2.entries.take(3).forEach { petType ->
                    SmallPetCardV2(
                        petType = petType,
                        isSelected = selectedPet == petType,
                        onClick = {
                            hapticManager?.lightClick()
                            onPetSelected(petType)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Row 2 (RACCOON, HAMSTER, PENGUIN)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PetTypeV2.entries.drop(3).take(3).forEach { petType ->
                    SmallPetCardV2(
                        petType = petType,
                        isSelected = selectedPet == petType,
                        onClick = {
                            hapticManager?.lightClick()
                            onPetSelected(petType)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 선택된 펫 특징 설명 (3줄) - 선택창과 버튼 정중앙
        Spacer(modifier = Modifier.weight(1f))
        if (selectedPet != null) {
            Text(
                text = stringResource(R.string.friend_characteristics),
                fontSize = 18.sp,
                color = MockupColors.TextMuted,
                textAlign = TextAlign.Center,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = getPetDescriptionV2(selectedPet),
                fontSize = 21.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        // Button
        MockupButton(
            text = stringResource(R.string.this_friend),
            onClick = onNext,
            enabled = selectedPet != null
        )
    }
}

/**
 * 펫 특징 설명 (3줄) - V1 레거시
 */
private fun getPetDescription(petType: PetType): String {
    return when (petType) {
        PetType.DOG1 -> PetTutorialStrings.descDog1()
        PetType.DOG2 -> PetTutorialStrings.descDog2()
        PetType.CAT1 -> PetTutorialStrings.descCat1()
        PetType.CAT2 -> PetTutorialStrings.descCat2()
        PetType.RAT -> PetTutorialStrings.descRat()
        PetType.BIRD -> PetTutorialStrings.descBird()
    }
}

/**
 * 펫 특징 설명 (3줄) - V2 새 펫들
 */
@Composable
private fun getPetDescriptionV2(petType: PetTypeV2): String {
    return when (petType) {
        PetTypeV2.SHIBA -> stringResource(R.string.pet_desc_shiba)
        PetTypeV2.CAT -> stringResource(R.string.pet_desc_cat)
        PetTypeV2.PIG -> stringResource(R.string.pet_desc_pig)
        PetTypeV2.RACCOON -> stringResource(R.string.pet_desc_raccoon)
        PetTypeV2.HAMSTER -> stringResource(R.string.pet_desc_hamster)
        PetTypeV2.PENGUIN -> stringResource(R.string.pet_desc_penguin)
    }
}

/**
 * Small pet card for selection - 원래 크기, 펫만 크게 (V1 레거시)
 */
@Composable
private fun SmallPetCard(
    petType: PetType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFD0D0D0) else MockupColors.CardBackground
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 3.dp else 2.dp,
            color = MockupColors.Border
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PetSprite(
                petType = petType,
                isWalking = false,
                size = 64.dp,
                monochrome = true,
                frameDurationMs = 500 // 애니메이션 속도 0.5배
            )
        }
    }
}

/**
 * Small pet card for selection - V2 펫 사용
 */
@Composable
private fun SmallPetCardV2(
    petType: PetTypeV2,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFD0D0D0) else MockupColors.CardBackground
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 3.dp else 2.dp,
            color = MockupColors.Border
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PetSpriteV2WithGlow(
                petType = petType,
                stage = PetGrowthStage.BABY,
                animationType = PetAnimationTypeV2.IDLE,
                size = 64.dp,
                monochrome = true,
                showGlow = false,
                applyDisplayScale = false  // 선택화면에서는 원본 크기 유지
            )
        }
    }
}

/**
 * Step 2: Pet Name Input - basic.png 목업 정확히 따름 (V2 펫 사용)
 */
@Composable
private fun PetNameInputStep(
    petType: PetTypeV2,
    currentName: String,
    onNameChanged: (String) -> Unit,
    onNext: () -> Unit,
    hapticManager: HapticManager?
) {
    val kenneyFont = rememberKenneyFont()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val speechText = stringResource(R.string.name_me)
    val displayPetSize = 140.dp
    val stripeWidth = 4.dp

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 72.dp),  // 3버튼 네비게이션 고려
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - 고정
        Text(
            text = "rebon",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display area - 고정 (스트라이프 배경)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = stripeWidth.toPx()
                    val stripeColor = Color(0xFFF0F0F0)
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = stripeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, y),
                            size = androidx.compose.ui.geometry.Size(size.width, stripeHeightPx)
                        )
                        y += stripeHeightPx * 2
                    }
                }
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SpeechBubble(text = speechText, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Pet sprite with glow (V2)
                PetSpriteV2WithGlow(
                    petType = petType,
                    stage = PetGrowthStage.BABY,
                    animationType = PetAnimationTypeV2.IDLE,
                    size = displayPetSize,
                    monochrome = true,
                    showGlow = true,
                    applyDisplayScale = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instruction text - 고정
        Text(
            text = stringResource(R.string.give_me_name),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name input field
        OutlinedTextField(
            value = currentName,
            onValueChange = { if (it.length <= 8) onNameChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.within_8_chars),
                        color = MockupColors.TextMuted,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MockupColors.TextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (currentName.isNotBlank()) onNext()
                }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MockupColors.Border,
                unfocusedBorderColor = MockupColors.Border,
                cursorColor = MockupColors.TextPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action button - 고정
        MockupButton(
            text = stringResource(R.string.okay_lets_go),
            onClick = {
                focusManager.clearFocus()
                onNext()
            },
            enabled = currentName.isNotBlank()
        )
    }
}

/**
 * Tutorial All-in-One: 3가지 튜토리얼 항목을 한 화면에 (V2 펫 사용)
 */
@Composable
private fun TutorialAllInOneStep(
    petType: PetTypeV2,
    petName: String,
    hapticManager: HapticManager?,
    onNext: () -> Unit  // 다음 단계로
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val displayPetSize = 140.dp
    val stripeWidth = 4.dp

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_start)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_start)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_start)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_start)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_start)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_start)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 72.dp),  // 3버튼 네비게이션 고려
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - 고정
        Text(
            text = "rebon",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display area - 고정 (스트라이프 배경)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = stripeWidth.toPx()
                    val stripeColor = Color(0xFFF0F0F0)
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = stripeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, y),
                            size = androidx.compose.ui.geometry.Size(size.width, stripeHeightPx)
                        )
                        y += stripeHeightPx * 2
                    }
                }
                .border(3.dp, MockupColors.Border, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SpeechBubble(text = speechText, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Pet sprite with glow (V2)
                PetSpriteV2WithGlow(
                    petType = petType,
                    stage = PetGrowthStage.BABY,
                    animationType = PetAnimationTypeV2.IDLE,
                    size = displayPetSize,
                    monochrome = true,
                    showGlow = true,
                    applyDisplayScale = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instruction text - 고정
        Text(
            text = stringResource(R.string.what_to_do_with, petName),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3가지 튜토리얼 항목
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TutorialItemRow(
                iconName = "icon_target",
                title = stringResource(R.string.tutorial_goal_title),
                description = stringResource(R.string.tutorial_goal_desc)
            )
            TutorialItemRow(
                iconName = "icon_boots",
                title = stringResource(R.string.tutorial_achieve_title),
                description = stringResource(R.string.tutorial_achieve_desc)
            )
            TutorialItemRow(
                iconName = "icon_lock",
                title = stringResource(R.string.tutorial_control_title),
                description = stringResource(R.string.tutorial_control_desc)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 시작하기 버튼만 (Google 로그인은 step 0에서 완료됨)
        MockupButton(
            text = stringResource(R.string.lets_start),
            onClick = {
                hapticManager?.click()
                onNext()
            }
        )
    }
}

/**
 * 튜토리얼 항목 Row
 */
@Composable
private fun TutorialItemRow(
    iconName: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Icon - 중앙 정렬, 더 어둡게
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF2D2D2D), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            PixelIcon(
                iconName = iconName,
                size = 28.dp,
                alpha = 1f
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MockupColors.TextSecondary
            )
        }
    }
}

// =====================================================
// 미니게임용 클래스들
// =====================================================
private enum class DinoGameState { IDLE, PLAYING, GAME_OVER }
private data class GameObstacle(val x: Float, val type: Int, val iconIndex: Int = 0)

// =====================================================
// STEP 0: Google Sign-In (데이터 백업)
// =====================================================
@Composable
private fun GoogleSignInStep(
    hapticManager: HapticManager?,
    onNext: () -> Unit,  // 신규 사용자: 펫 선택으로
    onDataRestored: () -> Unit  // 기존 사용자: 튜토리얼 스킵
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kenneyFont = rememberKenneyFont()
    val stripeWidth = 4.dp

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSignedIn by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Google Sign-In 함수 (Credential Manager 사용)
    fun performGoogleSignIn() {
        isLoading = true
        statusMessage = PetTutorialStrings.loggingIn()
        scope.launch {
            val result = GoogleSignInHelper.signIn(context)
            when (result) {
                is GoogleSignInHelper.SignInResult.Success -> {
                    val firebaseResult = GoogleSignInHelper.signInToFirebase(result.idToken)
                    if (firebaseResult.isSuccess) {
                        Log.d("GoogleSignIn", "Firebase sign-in successful")

                        // 🔥 Activity 재생성 대비: sync 전에 즉시 step 1 저장
                        val prefManager = PreferenceManager(context)
                        prefManager.saveTutorialCurrentStep(1)
                        Log.d("GoogleSignIn", "✅ Saved step 1 immediately after sign-in")

                        statusMessage = PetTutorialStrings.checkingData()

                        // Repository 동기화 및 데이터 확인
                        val app = context.applicationContext as WalkorWaitApp
                        app.userDataRepository.startSync()

                        // 동기화 완료 대기 (최대 5초 - 타임아웃 시 강제 진행)
                        var waitCount = 0
                        while (!app.userDataRepository.syncCompleted.value && waitCount < 50) {
                            delay(100)
                            waitCount++
                        }
                        val syncTimedOut = waitCount >= 50
                        Log.d("GoogleSignIn", "Sync wait completed - waited ${waitCount * 100}ms, syncCompleted: ${app.userDataRepository.syncCompleted.value}, timedOut: $syncTimedOut")

                        // 타임아웃 시 강제로 syncCompleted 표시
                        if (syncTimedOut) {
                            Log.w("GoogleSignIn", "⚠️ Sync timed out - forcing completion")
                        }

                        // 기존 데이터가 있는지 확인 (여러 소스에서 체크)
                        var tutorialCompleted = prefManager.isTutorialCompleted()
                        val petType = prefManager.getPetType()
                        val hasPetType = petType != null && petType != "DOG1"  // 기본값이 아닌 경우만
                        val hasLockedApps = prefManager.getLockedApps().isNotEmpty()
                        val streak = prefManager.getStreak()
                        val hasStreak = streak > 0
                        val petTotalSteps = prefManager.getPetTotalSteps()
                        val hasPetSteps = petTotalSteps > 0

                        // ChallengeManager에서 칭호 데이터도 확인
                        val challengePrefs = context.getSharedPreferences("challenge_prefs", android.content.Context.MODE_PRIVATE)
                        val unlockedTitles = challengePrefs.getStringSet("unlocked_titles", emptySet()) ?: emptySet()
                        val hasUnlockedTitles = unlockedTitles.isNotEmpty()

                        // 기존 사용자 판단: tutorialCompleted, petType, lockedApps, 칭호, streak 중 하나라도 있으면
                        var isExistingUser = tutorialCompleted || hasPetType || hasLockedApps || hasUnlockedTitles || hasStreak || hasPetSteps

                        Log.d("GoogleSignIn", "Data check (local) - tutorialCompleted: $tutorialCompleted, petType: $petType, hasPetType: $hasPetType, hasLockedApps: $hasLockedApps, hasStreak: $hasStreak, hasPetSteps: $hasPetSteps, hasUnlockedTitles: $hasUnlockedTitles, isExistingUser: $isExistingUser")

                        // 로컬에서 기존 사용자 판단 실패 시 Firebase에서 직접 확인
                        if (!isExistingUser) {
                            Log.d("GoogleSignIn", "🔍 Local check failed, checking Firebase directly...")
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                try {
                                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    // 부모 문서 확인
                                    val parentDoc = firestore.collection("users")
                                        .document(userId)
                                        .get()
                                        .await()

                                    val fbTutorialCompleted = parentDoc.getBoolean("tutorialCompleted") ?: false
                                    val fbPetType = parentDoc.getString("petType")
                                    val fbLockedApps = (parentDoc.get("lockedApps") as? List<*>)?.size ?: 0
                                    val fbUnlockedTitles = (parentDoc.get("unlockedTitles") as? List<*>)?.size ?: 0
                                    val fbPaidDeposit = parentDoc.getBoolean("paidDeposit") ?: false

                                    Log.d("GoogleSignIn", "🔍 Firebase parent doc - tutorialCompleted: $fbTutorialCompleted, petType: $fbPetType, lockedApps: $fbLockedApps, unlockedTitles: $fbUnlockedTitles, paidDeposit: $fbPaidDeposit")

                                    // settings 서브컬렉션도 확인
                                    val settingsDoc = firestore.collection("users")
                                        .document(userId)
                                        .collection("userData")
                                        .document("settings")
                                        .get()
                                        .await()

                                    val settingsTutorial = settingsDoc.getBoolean("tutorialCompleted") ?: false
                                    val settingsLockedApps = (settingsDoc.get("lockedApps") as? List<*>)?.size ?: 0
                                    val settingsStreak = settingsDoc.getLong("streak")?.toInt() ?: 0
                                    val settingsPetSteps = settingsDoc.getLong("petTotalSteps") ?: 0L

                                    Log.d("GoogleSignIn", "🔍 Firebase settings - tutorialCompleted: $settingsTutorial, lockedApps: $settingsLockedApps, streak: $settingsStreak, petTotalSteps: $settingsPetSteps")

                                    // Firebase에 기존 사용자 데이터가 있으면
                                    if (fbTutorialCompleted || settingsTutorial || fbPaidDeposit ||
                                        fbLockedApps > 0 || settingsLockedApps > 0 ||
                                        fbUnlockedTitles > 0 || settingsStreak > 0 || settingsPetSteps > 0 ||
                                        (fbPetType != null && fbPetType != "DOG1")) {

                                        Log.d("GoogleSignIn", "✅ Found existing user data in Firebase!")
                                        isExistingUser = true
                                        tutorialCompleted = fbTutorialCompleted || settingsTutorial

                                        // 동기화가 제대로 안됐으면 다시 시도
                                        if (!app.userDataRepository.syncCompleted.value) {
                                            Log.d("GoogleSignIn", "🔄 Retrying sync...")
                                            app.userDataRepository.startSync()
                                            // 추가 대기 (최대 3초)
                                            var retryCount = 0
                                            while (!app.userDataRepository.syncCompleted.value && retryCount < 30) {
                                                delay(100)
                                                retryCount++
                                            }
                                            Log.d("GoogleSignIn", "🔄 Retry sync completed after ${retryCount * 100}ms")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("GoogleSignIn", "❌ Firebase direct check failed: ${e.message}")
                                }
                            }
                        }

                        isSignedIn = true
                        isLoading = false
                        hapticManager?.success()

                        // Analytics 추적
                        AnalyticsManager.trackSettingsChanged("google_signin", "success")

                        // 기존 사용자면 튜토리얼 스킵
                        if (isExistingUser) {
                            // 먼저 Firebase 데이터를 로컬로 복원 (강제 재sync)
                            Log.d("GoogleSignIn", "🔄 Force re-sync for existing user after Google login")
                            app.userDataRepository.startSync()
                            var syncWaitCount = 0
                            while (!app.userDataRepository.syncCompleted.value && syncWaitCount < 50) {
                                delay(100)
                                syncWaitCount++
                            }
                            Log.d("GoogleSignIn", "🔄 Force re-sync completed after ${syncWaitCount * 100}ms")

                            // sync 후에도 tutorialCompleted가 false면 true로 수정
                            if (!prefManager.isTutorialCompleted()) {
                                prefManager.setTutorialCompleted(true)
                                app.userDataRepository.setTutorialCompleted(true)
                                Log.d("GoogleSignIn", "Fixed tutorialCompleted to true")
                            }
                            // 기존 데이터가 있으면 바로 메인으로
                            statusMessage = PetTutorialStrings.dataRestoreComplete()
                            delay(1000)
                            onDataRestored()
                        } else {
                            // 기존 데이터 없으면 펫 선택으로
                            statusMessage = PetTutorialStrings.loginComplete()
                            delay(500)
                            onNext()
                        }
                    } else {
                        errorMessage = PetTutorialStrings.firebaseLoginFailed()
                        statusMessage = null
                        isLoading = false
                    }
                }
                is GoogleSignInHelper.SignInResult.Error -> {
                    if (!result.isCancelled) {
                        errorMessage = result.message
                    }
                    statusMessage = null
                    isLoading = false
                }
            }
        }
    }

    // ===== 공룡 게임 스타일 미니게임 =====
    var gameState by remember { mutableStateOf(DinoGameState.IDLE) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }

    // Player physics
    var playerY by remember { mutableFloatStateOf(0f) }  // 0 = ground
    var velocityY by remember { mutableFloatStateOf(0f) }
    val gravity = 1800f  // pixels per second^2
    val jumpVelocity = -900f  // negative = up
    val groundY = 0f

    // Obstacles: list of (x position, type: 0=icon, 1=tree)
    var obstacles by remember { mutableStateOf(listOf<GameObstacle>()) }
    var gameSpeed by remember { mutableFloatStateOf(300f) }  // pixels per second
    val maxSpeed = 1000f  // 최대 속도 증가

    // Obstacle icons
    val iconList = listOf(
        R.drawable.social_icon_01,
        R.drawable.social_icon_02,
        R.drawable.social_icon_03,
        R.drawable.social_icon_04,
        R.drawable.social_icon_05,
        R.drawable.social_icon_06,
        R.drawable.social_icon_07,
        R.drawable.social_icon_08,
        R.drawable.social_icon_09,
        R.drawable.social_icon_10
    )

    // Game dimensions (in dp, converted to px in game loop)
    val playerSize = 60.dp
    val obstacleWidth = 28.dp
    val obstacleHeight = 28.dp  // 정사각형
    val treeWidth = 20.dp
    val treeHeight = 50.dp
    val cactusWidth = 20.dp   // 나무와 같은 크기
    val cactusHeight = 50.dp
    val rockWidth = 15.dp     // 바위 크기 증가
    val rockHeight = 20.dp
    val gameAreaWidth = 400.dp

    // Convert dp to px
    val density = LocalDensity.current
    val playerSizePx = with(density) { playerSize.toPx() }
    val obstacleWidthPx = with(density) { obstacleWidth.toPx() }
    val obstacleHeightPx = with(density) { obstacleHeight.toPx() }
    val treeWidthPx = with(density) { treeWidth.toPx() }
    val treeHeightPx = with(density) { treeHeight.toPx() }
    val gameAreaWidthPx = with(density) { gameAreaWidth.toPx() }
    val playerXPx = with(density) { 70.dp.toPx() }  // Player X position

    // Jump function
    fun jump() {
        if (playerY >= groundY - 1f) {  // On or near ground
            velocityY = jumpVelocity
            hapticManager?.click()
        }
    }

    // Start/Restart game
    fun startGame() {
        gameState = DinoGameState.PLAYING
        score = 0
        playerY = 0f
        velocityY = 0f
        obstacles = listOf()
        gameSpeed = 300f
        hapticManager?.click()
    }

    // Flying obstacle height
    val flyingHeightPx = with(density) { 50.dp.toPx() }

    // Collision detection
    fun checkCollision(): Boolean {
        val playerLeft = playerXPx
        val playerRight = playerXPx + playerSizePx * 0.6f
        val playerBottom = -playerY
        val playerTop = playerBottom + playerSizePx * 0.6f

        for (obstacle in obstacles) {
            // 배경 장식(나무, 선인장, 바위)은 충돌 없음
            if (obstacle.type == 1 || obstacle.type == 3 || obstacle.type == 4) continue

            val obsWidth = obstacleWidthPx
            val obsHeight = obstacleHeightPx

            val obsLeft = obstacle.x
            val obsRight = obstacle.x + obsWidth

            // 날아오는 아이콘(type=2)은 위에서
            val obsBottom = if (obstacle.type == 2) flyingHeightPx else 0f
            val obsTop = obsBottom + obsHeight

            // AABB collision (아이콘만)
            if (playerRight > obsLeft && playerLeft < obsRight &&
                playerTop > obsBottom && playerBottom < obsTop) {
                return true
            }
        }
        return false
    }

    // Game loop
    LaunchedEffect(gameState) {
        if (gameState == DinoGameState.PLAYING) {
            var lastTime = System.nanoTime()
            var obstacleSpawnTimer = 0f
            val minSpawnInterval = 0.5f  // seconds
            val maxSpawnInterval = 2.5f  // seconds
            var nextSpawnTime = (minSpawnInterval + Math.random() * (maxSpawnInterval - minSpawnInterval)).toFloat()

            while (gameState == DinoGameState.PLAYING) {
                val currentTime = System.nanoTime()
                val deltaTime = (currentTime - lastTime) / 1_000_000_000f  // Convert to seconds
                lastTime = currentTime

                // Update player physics
                velocityY += gravity * deltaTime
                playerY += velocityY * deltaTime

                // Ground collision
                if (playerY > groundY) {
                    playerY = groundY
                    velocityY = 0f
                }

                // Update obstacles
                obstacles = obstacles.map {
                    it.copy(x = it.x - gameSpeed * deltaTime)
                }.filter { it.x > -100f }  // Remove off-screen obstacles

                // Spawn new obstacles
                obstacleSpawnTimer += deltaTime
                if (obstacleSpawnTimer >= nextSpawnTime) {
                    obstacleSpawnTimer = 0f
                    nextSpawnTime = (minSpawnInterval + Math.random() * (maxSpawnInterval - minSpawnInterval)).toFloat()

                    // Random obstacle type: 40% 바닥 아이콘, 15% 나무, 15% 선인장, 15% 바위, 15% 날아오는 아이콘
                    val rand = Math.random()
                    val type = when {
                        rand < 0.40 -> 0   // 바닥 아이콘
                        rand < 0.55 -> 1   // 나무 (배경)
                        rand < 0.70 -> 3   // 선인장 (배경)
                        rand < 0.85 -> 4   // 바위 (배경)
                        else -> 2          // 날아오는 아이콘
                    }
                    val iconIndex = (Math.random() * iconList.size).toInt()
                    obstacles = obstacles + GameObstacle(gameAreaWidthPx + 50f, type, iconIndex)
                }

                // Update score
                score++

                // Increase speed gradually
                if (gameSpeed < maxSpeed) {
                    gameSpeed += 12f * deltaTime  // 난이도 증가 속도 2.4배
                }

                // Check collision
                if (checkCollision()) {
                    gameState = DinoGameState.GAME_OVER
                    if (score > highScore) {
                        highScore = score
                    }
                    hapticManager?.click()  // Game over feedback
                }

                delay(16)  // ~60 FPS
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Top: Ribbon icon + rebon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Ribbon icon (grayscale) - from drawable
            Image(
                painter = painterResource(id = R.drawable.rebon_icon_trans),
                contentDescription = "rebon",
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "rebon",
                fontSize = 36.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // Main text
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(R.string.pet_friend))
                }
                append(stringResource(R.string.digital_habit_title))
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Sub text
        Text(
            text = stringResource(R.string.login_subtitle),
            fontSize = 14.sp,
            color = MockupColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ===== 미니게임 영역 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
                .clickable {
                    when (gameState) {
                        DinoGameState.IDLE -> startGame()
                        DinoGameState.PLAYING -> jump()
                        DinoGameState.GAME_OVER -> startGame()
                    }
                }
        ) {
            // Score display (top right)
            if (gameState != DinoGameState.IDLE) {
                Text(
                    text = "SCORE: $score",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kenneyFont,
                    color = Color.Black
                )
            }

            // High score (top left)
            if (highScore > 0) {
                Text(
                    text = "HI: $highScore",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    fontSize = 12.sp,
                    fontFamily = kenneyFont,
                    color = Color.Black
                )
            }

            // Ground line - 픽셀 점선 패턴
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 8.dp)
                    .offset(y = (-30).dp)
            ) {
                val dotSize = 6f
                val gap = 6f
                var x = 0f
                while (x < size.width) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x, 0f),
                        size = Size(dotSize, size.height)
                    )
                    x += dotSize + gap
                }
            }

            // Game content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
            ) {
                // Obstacles
                obstacles.forEach { obstacle ->
                    val xDp = with(density) { obstacle.x.toDp() }

                    when (obstacle.type) {
                        0 -> {
                            // 바닥 아이콘
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = xDp, y = (-4).dp)
                                    .size(obstacleWidth, obstacleHeight)
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .background(Color.White, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = iconList[obstacle.iconIndex]),
                                    contentDescription = "아이콘",
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(Color.Black)
                                )
                            }
                        }
                        1 -> {
                            // 나무 (배경 장식)
                            Canvas(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = xDp, y = (-4).dp)
                                    .size(treeWidth, treeHeight)
                            ) {
                                // Tree trunk
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(size.width * 0.35f, size.height * 0.5f),
                                    size = Size(size.width * 0.3f, size.height * 0.5f)
                                )
                                // Tree top
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(0f, size.height * 0.1f),
                                    size = Size(size.width, size.height * 0.5f)
                                )
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(size.width * 0.15f, 0f),
                                    size = Size(size.width * 0.7f, size.height * 0.3f)
                                )
                            }
                        }
                        2 -> {
                            // 날아오는 아이콘 (위에서)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = xDp, y = (-54).dp)  // 위쪽에 배치
                                    .size(obstacleWidth, obstacleHeight)
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .background(Color.White, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = iconList[obstacle.iconIndex]),
                                    contentDescription = "아이콘",
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(Color.Black)
                                )
                            }
                        }
                        3 -> {
                            // 선인장 (배경 장식) - 나무의 1/2
                            Canvas(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = xDp, y = (-4).dp)
                                    .size(cactusWidth, cactusHeight)
                            ) {
                                // 선인장 몸통
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(size.width * 0.3f, size.height * 0.2f),
                                    size = Size(size.width * 0.4f, size.height * 0.8f)
                                )
                                // 왼쪽 팔
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(0f, size.height * 0.4f),
                                    size = Size(size.width * 0.3f, size.height * 0.15f)
                                )
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(0f, size.height * 0.25f),
                                    size = Size(size.width * 0.15f, size.height * 0.3f)
                                )
                                // 오른쪽 팔
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(size.width * 0.7f, size.height * 0.5f),
                                    size = Size(size.width * 0.3f, size.height * 0.15f)
                                )
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(size.width * 0.85f, size.height * 0.35f),
                                    size = Size(size.width * 0.15f, size.height * 0.3f)
                                )
                            }
                        }
                        4 -> {
                            // 바위 (배경 장식) - 나무의 1/4
                            Canvas(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = xDp, y = (-4).dp)
                                    .size(rockWidth, rockHeight)
                            ) {
                                // 바위 모양
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(0f, size.height * 0.3f),
                                    size = Size(size.width, size.height * 0.7f)
                                )
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(size.width * 0.2f, 0f),
                                    size = Size(size.width * 0.6f, size.height * 0.5f)
                                )
                            }
                        }
                    }
                }

                // Player (dog sprite)
                val playerYDp = with(density) { playerY.toDp() }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 70.dp, y = playerYDp + 12.dp)  // V2 펫 Y축 보정
                ) {
                    // 미니게임 플레이어 - SHIBA 사용
                    PetSpriteV2WithGlow(
                        petType = PetTypeV2.SHIBA,
                        stage = PetGrowthStage.BABY,
                        animationType = if (gameState == DinoGameState.PLAYING && playerY >= -1f)
                            PetAnimationTypeV2.WALK else PetAnimationTypeV2.IDLE,
                        size = playerSize,
                        monochrome = true,
                        showGlow = false,
                        applyDisplayScale = false
                    )
                }
            }

            // IDLE state overlay - 게임 요소는 보이게 하고 텍스트만 오버레이
            if (gameState == DinoGameState.IDLE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TAP TO START",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.tap_to_jump),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            // GAME OVER overlay
            if (gameState == DinoGameState.GAME_OVER) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GAME OVER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SCORE: $score",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = kenneyFont,
                            color = Color.Black
                        )
                        if (score >= highScore && score > 0) {
                            Text(
                                text = "NEW BEST!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "TAP TO RESTART",
                            fontSize = 14.sp,
                            fontFamily = kenneyFont,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Status or error message
        statusMessage?.let { status ->
            Text(
                text = status,
                fontSize = 16.sp,
                color = MockupColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Google 로그인 버튼 (필수) - 다마고치 스타일
        if (!isSignedIn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(Border.medium, MockupColors.Border, RoundedCornerShape(Radius.sm))
                    .background(MockupColors.Border, RoundedCornerShape(Radius.sm))
                    .clickable(enabled = !isLoading) {
                        hapticManager?.click()
                        errorMessage = null
                        performGoogleSignIn()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google 'G' 픽셀 아이콘
                        DrawableIcon(
                            iconName = "icon_google",
                            size = 20.dp,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.google_login),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = kenneyFont
                        )
                    }
                }
            }
        } else {
            // Signed in state - 성공 스타일
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(Border.medium, MockupColors.Blue, RoundedCornerShape(Radius.sm))
                    .background(MockupColors.Blue, RoundedCornerShape(Radius.sm)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PixelIcon(
                            iconName = "icon_check",
                            size = 20.dp,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.login_complete),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = kenneyFont
                        )
                    }
                }
            }
        }

        // Debug 모드에서만 표시되는 테스트 버튼
        if (BuildConfig.DEBUG && !isSignedIn) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFFFF6B6B), RoundedCornerShape(12.dp))
                    .clickable(enabled = !isLoading) {
                        hapticManager?.click()
                        // 로그인 없이 바로 펫 선택으로 진행
                        onNext()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.debug_test_without_login),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// =====================================================
// STEP 4: Permission Settings (권한 설정)
// =====================================================
@Composable
private fun PermissionSettingsStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var activityPermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(true) }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activityPermissionGranted = isGranted
        if (isGranted) {
            hapticManager?.success()
            // 권한 부여 후 바로 StepCounterService 시작 (WalkingTestStep에서 걸음 수 측정용)
            StepCounterService.start(context)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_permission)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_permission)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_permission)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_permission)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_permission)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_permission)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.permission_setup),
        buttonText = stringResource(R.string.next),
        onButtonClick = onNext,
        buttonEnabled = activityPermissionGranted,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        // 권한 카드들
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 걸음 측정 권한
            PermissionCard(
                iconName = "icon_boots",
                title = stringResource(R.string.step_measurement),
                description = stringResource(R.string.measures_your_steps),
                isGranted = activityPermissionGranted,
                onRequest = {
                    hapticManager?.lightClick()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        activityPermissionGranted = true
                    }
                }
            )

            // 알림 권한
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionCard(
                    iconName = "icon_bell",
                    title = stringResource(R.string.notifications),
                    description = stringResource(R.string.notifies_progress),
                    isGranted = notificationPermissionGranted,
                    onRequest = {
                        hapticManager?.lightClick()
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 안내 텍스트
        Text(
            text = stringResource(R.string.step_permission_required),
            fontSize = 14.sp,
            color = MockupColors.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Permission card component
 */
@Composable
private fun PermissionCard(
    iconName: String,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isGranted) Color(0xFFE8F5E9) else Color.White,
                RoundedCornerShape(12.dp)
            )
            .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PixelIcon(iconName = iconName, size = 24.dp)
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MockupColors.TextSecondary
                )
            }
        }

        if (isGranted) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        } else {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Border),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(PetTutorialStrings.allow(), fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

// =====================================================
// STEP 5: Fitness App Connection (피트니스 앱 연결)
// =====================================================
@Composable
private fun FitnessConnectionStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }
    val scope = rememberCoroutineScope()

    var installedApps by remember { mutableStateOf<List<com.moveoftoday.walkorwait.FitnessApp>>(emptyList()) }
    var isHealthConnectAvailable by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.createPermissionRequestContract()
    ) { _ ->
        scope.launch {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                isConnecting = false
                val firstApp = installedApps.firstOrNull()
                preferenceManager.setUseHealthConnect(true)
                preferenceManager.setHealthConnectConnected(true)
                preferenceManager.setConnectedFitnessAppName(firstApp?.appName ?: "")
                // 서비스 재시작하여 Health Connect 모드로 전환
                StepCounterService.stop(context)
                StepCounterService.start(context)
                hapticManager?.success()
                delay(500)
                onNext()
            } else {
                isConnecting = false
            }
        }
    }

    LaunchedEffect(Unit) {
        isHealthConnectAvailable = healthConnectManager.isAvailable()
        installedApps = healthConnectManager.getInstalledFitnessApps()
        if (isHealthConnectAvailable) {
            hasPermissions = healthConnectManager.hasAllPermissions()
            if (hasPermissions) {
                delay(1000)
                onNext()
            }
        }
    }

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> PetTutorialStrings.fitnessLoyalSpeech()
        PetPersonalityV2.TSUNDERE -> PetTutorialStrings.fitnessTsundereSpeech()
        PetPersonalityV2.FOODIE -> PetTutorialStrings.fitnessFoodieSpeech()
        PetPersonalityV2.PLAYFUL -> PetTutorialStrings.fitnessPlayfulSpeech()
        PetPersonalityV2.TIMID -> PetTutorialStrings.fitnessTimidSpeech()
        PetPersonalityV2.CLUMSY -> PetTutorialStrings.fitnessClumsySpeech()
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = PetTutorialStrings.fitnessAppConnection(),
        buttonText = PetTutorialStrings.doLater(),
        onButtonClick = {
            hapticManager?.click()
            onNext()
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        if (installedApps.isNotEmpty()) {
            // 발견된 앱 표시
            Text(
                text = PetTutorialStrings.foundFitnessApps(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            installedApps.take(2).forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(app.appName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MockupColors.TextPrimary)
                            Text(PetTutorialStrings.installed(), fontSize = 12.sp, color = MockupColors.TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isHealthConnectAvailable) {
                Button(
                    onClick = {
                        isConnecting = true
                        permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isConnecting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Blue)
                ) {
                    Text(
                        text = if (isConnecting) PetTutorialStrings.connecting() else PetTutorialStrings.connect(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // 앱이 없을 때
            Text(
                text = PetTutorialStrings.noFitnessAppFound(),
                fontSize = 14.sp,
                color = MockupColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =====================================================
// STEP 5: Accessibility Consent (접근성 권한 동의 - Google Play 정책 준수)
// =====================================================
// Google Play 정책 요구사항:
// 1. 권한 요청 전 명시적 공개 대화상자 제시
// 2. 사용자가 동의 의사를 확실하게 표현하도록 요구 (체크박스)
// 3. 대화상자 나가기를 동의로 해석 금지 (BackHandler)
// 4. 두 개의 버튼 필수 (동의/거부)
// 5. 동의 후 바로 시스템 설정으로 이동
// 6. 상세 내용은 팝업 다이얼로그로 표시 (기존 튜토리얼 스타일 유지)
@Composable
private fun AccessibilityConsentStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onAgree: () -> Unit
) {
    val context = LocalContext.current
    var showDeclineDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAccessibilityGuide by remember { mutableStateOf(false) }

    // 체크박스 상태 (상세 다이얼로그에서 4개 체크 후 메인 화면에서 최종 동의)
    var detailsChecked by remember { mutableStateOf(false) }  // 상세 내용 확인 완료
    var finalConsent by remember { mutableStateOf(false) }    // 최종 동의

    val canProceed = detailsChecked && finalConsent

    // 접근성 서비스 활성화 확인 (설정에서 돌아왔을 때)
    LaunchedEffect(Unit) {
        while (true) {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (enabledServices?.contains("com.moveoftoday.walkorwait") == true) {
                hapticManager?.success()
                delay(500)
                onAgree()
                break
            }
            delay(1000)
        }
    }

    // Google Play 정책: 뒤로가기로 동의 화면을 우회할 수 없음
    androidx.activity.compose.BackHandler(enabled = true) {
        showDeclineDialog = true
    }

    // 펫 성격별 말풍선 텍스트
    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> when (java.util.Locale.getDefault().language) {
            "ko" -> "...앱 차단 기능을 위해\n접근성 권한이 필요해."
            else -> "...Accessibility permission\nneeded for app blocking."
        }
        PetPersonalityV2.TSUNDERE -> when (java.util.Locale.getDefault().language) {
            "ko" -> "흥, 권한 설정 안 하면\n차단 못 해주는 거 알지?"
            else -> "Hmph, you know I can't block\napps without permission, right?"
        }
        PetPersonalityV2.FOODIE -> when (java.util.Locale.getDefault().language) {
            "ko" -> "앱 차단하려면 권한이\n필요하다구~! 맛있겠다..."
            else -> "Need permission for blocking~!\nLooks delicious..."
        }
        PetPersonalityV2.PLAYFUL -> when (java.util.Locale.getDefault().language) {
            "ko" -> "야호~! 권한 주면\n앱 차단해줄게!ㅋㅋ"
            else -> "Yay~! Give permission and\nI'll block apps! lol"
        }
        PetPersonalityV2.TIMID -> when (java.util.Locale.getDefault().language) {
            "ko" -> "저... 권한이 필요해요...\n무서우면 상세 보기 눌러주세요..."
            else -> "Um... I need permission...\nTap details if worried..."
        }
        PetPersonalityV2.CLUMSY -> when (java.util.Locale.getDefault().language) {
            "ko" -> "어... 권한 설정해야\n차단할 수 있어...아마도?"
            else -> "Uh... need to set permission\nto block...I think?"
        }
    }

    // 기존 튜토리얼 스타일의 메인 레이아웃
    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = when (java.util.Locale.getDefault().language) {
            "ko" -> "접근성 권한 설정"
            "ja" -> "アクセシビリティ権限設定"
            "zh" -> "无障碍权限设置"
            "es" -> "Configurar accesibilidad"
            else -> "Accessibility Permission"
        },
        buttonText = "",  // 버튼을 content 영역에 직접 배치 (빈 문자열이면 TutorialStepLayout 버튼 숨김)
        onButtonClick = { },
        buttonEnabled = false,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        // 공개 문구 박스 (Google 권장 형식 - 핵심 내용 바로 표시)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF9E6), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFFFFB800), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Google 권장 형식 공개 문구
            Text(
                text = AccessibilityConsentStrings.shortDescription(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 수집하지 않는 정보 (핵심 내용 바로 표시)
            Text(
                text = when (java.util.Locale.getDefault().language) {
                    "ko" -> "수집하지 않는 정보:"
                    "ja" -> "収集しない情報:"
                    "zh" -> "不收集的信息:"
                    "es" -> "Información NO recopilada:"
                    else -> "Information NOT collected:"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = when (java.util.Locale.getDefault().language) {
                    "ko" -> "• 화면 내용  • 개인 데이터  • 비밀번호"
                    "ja" -> "• 画面内容  • 個人データ  • パスワード"
                    "zh" -> "• 屏幕内容  • 个人数据  • 密码"
                    "es" -> "• Contenido  • Datos personales  • Contraseñas"
                    else -> "• Screen content  • Personal data  • Passwords"
                },
                fontSize = 11.sp,
                color = Color.Black,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 상세 보기 버튼 + 상태
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    hapticManager?.click()
                    showDetailsDialog = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (detailsChecked) MockupColors.TextPrimary else MockupColors.Border
                )
            ) {
                if (detailsChecked) {
                    Text("${UnicodeSymbols.CHECK} ", color = MockupColors.TextPrimary, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = if (detailsChecked) {
                        when (java.util.Locale.getDefault().language) {
                            "ko" -> "확인 완료"
                            else -> "Reviewed"
                        }
                    } else {
                        AccessibilityConsentStrings.viewDetails()
                    },
                    fontSize = 14.sp,
                    fontWeight = if (detailsChecked) FontWeight.Bold else FontWeight.Normal,
                    color = MockupColors.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 최종 동의 체크박스
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (finalConsent) Color(0xFFF0F0F0) else Color.White,
                    RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (finalConsent) MockupColors.TextPrimary else MockupColors.Border,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(enabled = detailsChecked) {
                    if (detailsChecked) finalConsent = !finalConsent
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = finalConsent,
                onCheckedChange = { if (detailsChecked) finalConsent = it },
                enabled = detailsChecked,
                colors = CheckboxDefaults.colors(
                    checkedColor = MockupColors.TextPrimary,
                    uncheckedColor = if (detailsChecked) MockupColors.TextMuted else MockupColors.TextMuted.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AccessibilityConsentStrings.consentCheckboxLabel(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    finalConsent -> MockupColors.TextPrimary
                    detailsChecked -> MockupColors.TextPrimary
                    else -> MockupColors.TextMuted
                },
                lineHeight = 16.sp
            )
        }

        // 버튼 2개 (Google Play 정책: 동의/거부 버튼 2개 필수)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 거부 버튼 (왼쪽)
            OutlinedButton(
                onClick = {
                    hapticManager?.click()
                    showDeclineDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MockupColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MockupColors.TextSecondary
                )
            ) {
                Text(
                    text = AccessibilityConsentStrings.declineButton(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 동의 버튼 (오른쪽)
            Button(
                onClick = {
                    if (canProceed) {
                        hapticManager?.success()
                        showAccessibilityGuide = true
                    }
                },
                enabled = canProceed,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = when (java.util.Locale.getDefault().language) {
                        "ko" -> "동의"
                        "ja" -> "同意"
                        "zh" -> "同意"
                        "es" -> "Acepto"
                        else -> "Agree"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // 상세 내용 팝업 다이얼로그 (4개 체크박스 포함)
    if (showDetailsDialog) {
        AccessibilityDetailsPopupDialog(
            onDismiss = { showDetailsDialog = false },
            onConfirm = {
                detailsChecked = true
                showDetailsDialog = false
            }
        )
    }

    // 뒤로가기/거부 시 표시되는 다이얼로그
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = {
                Text(
                    text = AccessibilityConsentStrings.declinedTitle(),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = AccessibilityConsentStrings.declinedMessage(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text(AccessibilityConsentStrings.understand())
                }
            }
        )
    }

    // 접근성 설정 가이드 다이얼로그
    if (showAccessibilityGuide) {
        AccessibilityGuideDialog(
            onConfirm = {
                showAccessibilityGuide = false
                PreferenceManager(context).setAwaitingAccessibilityReturn(true)
                openAccessibilitySettingsWithHighlight(context)
            },
            onDismiss = { showAccessibilityGuide = false }
        )
    }
}

/**
 * 접근성 설정 딥링크 (rebon 서비스 자동 하이라이트)
 */
private fun openAccessibilitySettingsWithHighlight(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    try {
        val componentName = ComponentName(
            context.packageName,
            "com.moveoftoday.walkorwait.AppBlockService"
        )
        val flattenedName = componentName.flattenToString()
        // Android 시스템이 해당 서비스를 하이라이트/스크롤하도록 힌트 제공
        intent.putExtra(":settings:fragment_args_key", flattenedName)
        val bundle = Bundle()
        bundle.putString(":settings:fragment_args_key", flattenedName)
        intent.putExtra(":settings:show_fragment_args", bundle)
    } catch (_: Exception) {
        // 딥링크 실패 시 기본 접근성 설정으로 이동
    }
    context.startActivity(intent)
}

/**
 * 접근성 설정 가이드 다이얼로그 (설정 열기 전 안내)
 */
@Composable
private fun AccessibilityGuideDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val lang = java.util.Locale.getDefault().language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (lang) {
                    "ko" -> "설정 방법 안내"
                    "ja" -> "設定方法"
                    "zh" -> "设置说明"
                    "es" -> "Instrucciones"
                    else -> "How to Enable"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Step 1
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "1. ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MockupColors.TextPrimary
                    )
                    Text(
                        text = when (lang) {
                            "ko" -> "설치된 앱 목록에서 \"rebon\" 을 찾아주세요"
                            "ja" -> "インストール済みアプリから「rebon」を見つけてください"
                            "zh" -> "在已安装应用中找到 \"rebon\""
                            "es" -> "Busca \"rebon\" en las apps instaladas"
                            else -> "Find \"rebon\" in the installed apps list"
                        },
                        fontSize = 15.sp,
                        color = MockupColors.TextPrimary,
                        lineHeight = 22.sp
                    )
                }
                // Step 2
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "2. ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MockupColors.TextPrimary
                    )
                    Text(
                        text = when (lang) {
                            "ko" -> "rebon을 눌러서 토글을 켜주세요"
                            "ja" -> "rebonをタップしてトグルをONにしてください"
                            "zh" -> "点击 rebon 并打开开关"
                            "es" -> "Toca rebon y activa el interruptor"
                            else -> "Tap rebon and turn on the toggle"
                        },
                        fontSize = 15.sp,
                        color = MockupColors.TextPrimary,
                        lineHeight = 22.sp
                    )
                }
                // 자동 복귀 안내
                Text(
                    text = when (lang) {
                        "ko" -> "${UnicodeSymbols.CHECK} 켜면 자동으로 돌아옵니다"
                        "ja" -> "${UnicodeSymbols.CHECK} ONにすると自動で戻ります"
                        "zh" -> "${UnicodeSymbols.CHECK} 打开后会自动返回"
                        "es" -> "${UnicodeSymbols.CHECK} Volverás automáticamente"
                        else -> "${UnicodeSymbols.CHECK} You'll return automatically"
                    },
                    fontSize = 13.sp,
                    color = MockupColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = when (lang) {
                        "ko" -> "설정으로 이동"
                        "ja" -> "設定へ移動"
                        "zh" -> "前往设置"
                        "es" -> "Ir a ajustes"
                        else -> "Go to Settings"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (lang) {
                        "ko" -> "취소"
                        "ja" -> "キャンセル"
                        "zh" -> "取消"
                        "es" -> "Cancelar"
                        else -> "Cancel"
                    },
                    color = MockupColors.TextMuted
                )
            }
        }
    )
}

/**
 * 접근성 상세 내용 팝업 다이얼로그 (Google Play 정책 필수 내용 + 4개 체크박스)
 */
@Composable
private fun AccessibilityDetailsPopupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }
    var check4 by remember { mutableStateOf(false) }
    val allChecked = check1 && check2 && check3 && check4

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (java.util.Locale.getDefault().language) {
                    "ko" -> "AccessibilityService API 상세"
                    "ja" -> "AccessibilityService API 詳細"
                    "zh" -> "AccessibilityService API 详情"
                    "es" -> "Detalles de AccessibilityService API"
                    else -> "AccessibilityService API Details"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Google 권장 형식 공개 문구
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9E6), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = AccessibilityConsentStrings.prominentDisclosure(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 섹션 1 + 체크박스
                Text(
                    text = AccessibilityConsentStrings.sectionDataCollected(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = AccessibilityConsentStrings.dataCollectedContent(),
                    fontSize = 11.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { check1 = !check1 }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = check1,
                        onCheckedChange = { check1 = it },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AccessibilityConsentStrings.checkbox1DataCollection(),
                        fontSize = 11.sp,
                        color = MockupColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 섹션 2 + 체크박스
                Text(
                    text = AccessibilityConsentStrings.sectionDataUsage(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = AccessibilityConsentStrings.dataUsageContent(),
                    fontSize = 11.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { check2 = !check2 }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = check2,
                        onCheckedChange = { check2 = it },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AccessibilityConsentStrings.checkbox2DataUsage(),
                        fontSize = 11.sp,
                        color = MockupColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 섹션 3
                Text(
                    text = AccessibilityConsentStrings.sectionDataNotCollected(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = AccessibilityConsentStrings.dataNotCollectedContent(),
                    fontSize = 11.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 섹션 4 + 체크박스
                Text(
                    text = AccessibilityConsentStrings.sectionWithdrawal(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = AccessibilityConsentStrings.withdrawalContent(),
                    fontSize = 11.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { check3 = !check3 }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = check3,
                        onCheckedChange = { check3 = it },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AccessibilityConsentStrings.checkbox3Withdrawal(),
                        fontSize = 11.sp,
                        color = MockupColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 최종 확인 체크박스
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (check4) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { check4 = !check4 }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = check4,
                        onCheckedChange = { check4 = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AccessibilityConsentStrings.checkbox4FinalConsent(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (check4) Color(0xFF2E7D32) else MockupColors.TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = allChecked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockupColors.TextPrimary,
                    disabledContainerColor = MockupColors.TextMuted.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = when (java.util.Locale.getDefault().language) {
                        "ko" -> "확인 완료"
                        "ja" -> "確認完了"
                        "zh" -> "确认完成"
                        "es" -> "Confirmar"
                        else -> "Confirm"
                    },
                    color = if (allChecked) Color.White else MockupColors.TextMuted
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (java.util.Locale.getDefault().language) {
                        "ko" -> "닫기"
                        "ja" -> "閉じる"
                        "zh" -> "关闭"
                        "es" -> "Cerrar"
                        else -> "Close"
                    },
                    color = MockupColors.TextSecondary
                )
            }
        }
    )
}

// 공개 섹션 컴포넌트
@Composable
private fun DisclosureSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, MockupColors.Border, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 11.sp,
            color = MockupColors.TextSecondary,
            lineHeight = 16.sp
        )
    }
}

// 동의 체크박스 컴포넌트
@Composable
private fun ConsentCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MockupColors.TextPrimary,
                uncheckedColor = MockupColors.TextMuted
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (checked) MockupColors.TextPrimary else MockupColors.TextSecondary,
            lineHeight = 15.sp
        )
    }
}

// =====================================================
// STEP 6: Accessibility (접근성 권한)
// =====================================================
@Composable
private fun AccessibilityStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(true) }
    var showAccessibilityGuide by remember { mutableStateOf(false) }

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

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_accessibility)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_accessibility)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_accessibility)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_accessibility)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_accessibility)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_accessibility)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.app_control_setup),
        buttonText = stringResource(R.string.go_to_settings),
        onButtonClick = {
            hapticManager?.click()
            showAccessibilityGuide = true
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        // 설정 방법 안내
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.how_to_setup),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = stringResource(R.string.accessibility_instructions),
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.auto_proceed_when_on),
            fontSize = 13.sp,
            color = MockupColors.TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }

    // 접근성 설정 가이드 다이얼로그
    if (showAccessibilityGuide) {
        AccessibilityGuideDialog(
            onConfirm = {
                showAccessibilityGuide = false
                PreferenceManager(context).setAwaitingAccessibilityReturn(true)
                openAccessibilitySettingsWithHighlight(context)
            },
            onDismiss = { showAccessibilityGuide = false }
        )
    }
}

// =====================================================
// STEP 7: App Selection (앱 선택)
// =====================================================
@Composable
private fun AppSelectionStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val appsByCategory = remember { AppUtils.getInstalledAppsByCategory(context) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var expandedCategories by remember { mutableStateOf(setOf<AppCategory>()) }

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_app)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_app)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_app)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_app)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_app)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_app)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.select_apps_to_control),
        buttonText = if (selectedApps.isEmpty()) stringResource(R.string.select_at_least_one) else stringResource(R.string.next_with_count, selectedApps.size),
        onButtonClick = {
            hapticManager?.success()
            preferenceManager.saveLockedApps(selectedApps)
            onNext()
        },
        buttonEnabled = selectedApps.isNotEmpty(),
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        if (selectedApps.isNotEmpty()) {
            Text(
                text = stringResource(R.string.apps_selected_count, selectedApps.size),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 앱 카테고리 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            appsByCategory.forEach { (category, apps) ->
                item(key = "header_$category") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            hapticManager?.lightClick()
                            expandedCategories = if (category in expandedCategories) {
                                expandedCategories - category
                            } else {
                                expandedCategories + category
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MockupColors.Border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(category.displayNameRes),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = if (category in expandedCategories) "▼" else "▶",
                                fontSize = 12.sp,
                                color = MockupColors.TextMuted
                            )
                        }
                    }
                }

                if (category in expandedCategories) {
                    items(items = apps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            app.icon?.let {
                                androidx.compose.foundation.Image(
                                    bitmap = it,
                                    contentDescription = app.appName,
                                    modifier = Modifier.size(32.dp),
                                    colorFilter = ColorFilter.colorMatrix(
                                        ColorMatrix().apply { setToSaturation(0f) }
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = app.appName,
                                fontSize = 13.sp,
                                color = MockupColors.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = selectedApps.contains(app.packageName),
                                onCheckedChange = { checked ->
                                    hapticManager?.lightClick()
                                    selectedApps = if (checked) {
                                        selectedApps + app.packageName
                                    } else {
                                        selectedApps - app.packageName
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MockupColors.Border
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: AppCategory): String {
    return ""
}

// =====================================================
// STEP 8: Test Blocking (차단 테스트)
// =====================================================
@Composable
private fun TestBlockingStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    // 저장된 상태 불러오기 (앱 나갔다 돌아왔을 때 상태 유지)
    var testStarted by remember { mutableStateOf(preferenceManager.isBlockingTestStarted()) }
    var canProceed by remember { mutableStateOf(testStarted) }

    // 백그라운드 갔다 돌아왔는지 감지
    DisposableEffect(Unit) {
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    // 앱에서 나감 - 상태 저장
                    testStarted = true
                    preferenceManager.setBlockingTestStarted(true)
                }
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    // 앱으로 돌아옴 - 저장된 상태 확인
                    if (preferenceManager.isBlockingTestStarted() && !canProceed) {
                        testStarted = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    // 테스트 시작 후 3초 뒤 진행 가능
    LaunchedEffect(testStarted) {
        if (testStarted && !canProceed) {
            delay(3000)
            hapticManager?.success()
            canProceed = true
        }
    }

    val speechText = when {
        canProceed -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_test_done)
            PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_test_done)
            PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_test_done)
            PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_test_done)
            PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_test_done)
            PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_test_done)
        }
        testStarted -> stringResource(R.string.checking)
        else -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_test)
            PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_test)
            PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_test)
            PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_test)
            PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_test)
            PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_test)
        }
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = if (canProceed) stringResource(R.string.test_complete) else stringResource(R.string.app_blocking_test),
        buttonText = if (canProceed) stringResource(R.string.next) else stringResource(R.string.try_launching_app),
        onButtonClick = {
            if (canProceed) {
                hapticManager?.click()
                onNext()
            }
        },
        buttonEnabled = canProceed,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (canProceed) {
                Text(
                    text = stringResource(R.string.blocking_test_done),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = stringResource(R.string.now_try_walking),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )
            } else {
                Text(
                    text = stringResource(R.string.test_method),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = stringResource(R.string.test_instructions),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// =====================================================
// STEP 9: Goal Input (목표 설정)
// =====================================================
@Composable
private fun GoalInputStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    var stepsSliderValue by remember { mutableFloatStateOf(60f) }

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_goal)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_goal)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_goal)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_goal)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_goal)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_goal)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.goal_setting),
        buttonText = stringResource(R.string.next),
        onButtonClick = {
            hapticManager?.success()
            preferenceManager.saveGoal(stepsSliderValue.toInt())
            preferenceManager.saveGoalUnit("steps")
            onNext()
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 값 표시
            Text(
                text = stringResource(R.string.steps_count, stepsSliderValue.toInt()),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.trial_range),
                fontSize = 14.sp,
                color = MockupColors.TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 슬라이더
            Slider(
                value = stepsSliderValue,
                onValueChange = {
                    stepsSliderValue = it
                    hapticManager?.lightClick()
                },
                valueRange = 50f..70f,
                steps = 19,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MockupColors.Border,
                    activeTrackColor = MockupColors.Border,
                    inactiveTrackColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 안내
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.when_goal_achieved),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = stringResource(R.string.apps_unblocked),
                    fontSize = 13.sp,
                    color = MockupColors.TextSecondary
                )
            }
        }
    }
}

// =====================================================
// STEP 10: Control Days (제어 요일)
// =====================================================
@Composable
private fun ControlDaysStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // Mon-Fri
    val dayNames = listOf(
        stringResource(R.string.day_sun),
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat)
    )

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_days)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_days)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_days)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_days)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_days)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_days)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.control_days_select),
        buttonText = stringResource(R.string.next),
        onButtonClick = {
            hapticManager?.success()
            preferenceManager.saveControlDays(selectedDays)
            onNext()
        },
        buttonEnabled = selectedDays.isNotEmpty(),
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        // 요일 선택
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayNames.forEachIndexed { index, day ->
                val isSelected = selectedDays.contains(index)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MockupColors.TextPrimary else MockupColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            hapticManager?.lightClick()
                            selectedDays = if (checked) {
                                selectedDays + index
                            } else {
                                selectedDays - index
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MockupColors.Border
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 추천
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.recommend_weekdays),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            }
            Text(
                text = stringResource(R.string.weekend_free),
                fontSize = 13.sp,
                color = MockupColors.TextSecondary
            )
        }
    }
}

// =====================================================
// STEP 11: Block Time (차단 시간대)
// =====================================================
@Composable
private fun BlockTimeStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    var selectedPeriods by remember { mutableStateOf(setOf("morning", "afternoon", "evening", "night")) }
    val periodMorning = stringResource(R.string.period_morning)
    val periodAfternoon = stringResource(R.string.period_afternoon)
    val periodEvening = stringResource(R.string.period_evening)
    val periodNight = stringResource(R.string.period_night)
    val periods = listOf(
        "morning" to periodMorning,
        "afternoon" to periodAfternoon,
        "evening" to periodEvening,
        "night" to periodNight
    )

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_time)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_time)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_time)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_time)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_time)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_time)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.blocking_time),
        buttonText = stringResource(R.string.next),
        onButtonClick = {
            hapticManager?.success()
            preferenceManager.saveBlockingPeriods(selectedPeriods)
            onNext()
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        // 시간대 선택
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            periods.forEach { (periodId, label) ->
                val isSelected = selectedPeriods.contains(periodId)
                Card(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    onClick = {
                        hapticManager?.lightClick()
                        selectedPeriods = if (isSelected) {
                            selectedPeriods - periodId
                        } else {
                            selectedPeriods + periodId
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE0E0E0) else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MockupColors.Border else Color(0xFFE0E0E0)
                    )
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MockupColors.TextPrimary,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 안내
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(iconName = "icon_light_bulb", size = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            }
            Text(
                text = stringResource(R.string.unselected_not_blocked),
                fontSize = 13.sp,
                color = MockupColors.TextSecondary
            )
        }
    }
}

// =====================================================
// STEP 12: Walking Test (걷기 테스트)
// =====================================================
@Composable
private fun WalkingTestStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.moveoftoday.walkorwait.WalkorWaitApp
    val repository = app.userDataRepository

    // 튜토리얼 단계에서는 무조건 기본 센서 사용 (Health Connect 비활성화)
    val useHealthConnect = false  // 강제로 기본 센서 사용
    val healthConnectManager: HealthConnectManager? = null

    var baselineSteps by remember { mutableIntStateOf(repository.getTodaySteps()) }
    var currentSteps by remember { mutableIntStateOf(0) }
    var previousSteps by remember { mutableIntStateOf(0) }  // 실시간 햅틱용
    var manualOffset by remember { mutableIntStateOf(0) }  // 걷기 어려울 때 버튼용 수동 오프셋
    val targetSteps = repository.getGoal()
    var goalAchieved by remember { mutableStateOf(false) }
    val notificationHelper = remember { com.moveoftoday.walkorwait.NotificationHelper(context) }

    // 튜토리얼 진입 시 Health Connect 강제 비활성화
    var originalUseHealthConnect by remember { mutableStateOf(false) }

    // 서비스 재시작을 LaunchedEffect에서 처리 (비동기)
    LaunchedEffect(Unit) {
        // 이전 Health Connect 설정 백업
        originalUseHealthConnect = preferenceManager.useHealthConnect()
        android.util.Log.d("WalkingTest", "💾 Backup original useHealthConnect: $originalUseHealthConnect")

        // 강제로 기본 센서 사용하도록 설정
        preferenceManager.setUseHealthConnect(false)
        android.util.Log.d("WalkingTest", "🔧 Forced useHealthConnect = false for tutorial")

        // 센서 초기 걸음수 리셋 (새로운 측정 시작)
        preferenceManager.saveInitialSteps(-1)
        android.util.Log.d("WalkingTest", "🔄 Reset initialSteps to -1")

        // StepCounterService 재시작 (설정 변경 반영)
        StepCounterService.stop(context)
        kotlinx.coroutines.delay(500)  // 서비스 완전 종료 대기 (비동기)
        StepCounterService.start(context)
        android.util.Log.d("WalkingTest", "🔄 Restarted StepCounterService with basic sensor")
    }

    // 튜토리얼 종료 시 원래 설정 복원
    DisposableEffect(Unit) {
        onDispose {
            // 원래 설정 복원
            preferenceManager.setUseHealthConnect(originalUseHealthConnect)
            android.util.Log.d("WalkingTest", "🔙 Restored useHealthConnect to: $originalUseHealthConnect")

            // StepCounterService 재시작 (코루틴 스코프에서 비동기로)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                StepCounterService.stop(context)
                kotlinx.coroutines.delay(500)
                StepCounterService.start(context)
                android.util.Log.d("WalkingTest", "🔄 Restarted StepCounterService after tutorial")
            }
        }
    }

    // Baseline 초기화 (서비스 재시작 완료 후)
    LaunchedEffect(Unit) {
        // 서비스 시작 대기 후 baseline 업데이트 (stop 500ms + start 초기화 대기)
        kotlinx.coroutines.delay(1500)
        baselineSteps = repository.getTodaySteps()
        android.util.Log.d("WalkingTest", "📊 Sensor baseline: $baselineSteps")
    }

    LaunchedEffect(Unit) {
        while (!goalAchieved) {
            val rawSteps = if (useHealthConnect && healthConnectManager != null) {
                // Health Connect에서 직접 조회 (5초 간격)
                try {
                    val steps = healthConnectManager.getTodaySteps()
                    preferenceManager.saveTodaySteps(steps) // 로컬에도 저장
                    android.util.Log.d("WalkingTest", "Health Connect steps: $steps")
                    steps
                } catch (e: Exception) {
                    android.util.Log.e("WalkingTest", "Health Connect error: ${e.message}")
                    repository.getTodaySteps()
                }
            } else {
                val steps = repository.getTodaySteps()
                android.util.Log.d("WalkingTest", "Sensor steps: $steps, baseline: $baselineSteps")
                steps
            }

            val newSteps = maxOf(0, rawSteps - baselineSteps) + manualOffset

            // 실시간 걸음 증가 햅틱 (보고 있을 때)
            if (newSteps > previousSteps && newSteps > 0) {
                hapticManager?.lightClick()
                android.util.Log.d("WalkingTest", "👟 Step detected: $previousSteps → $newSteps (haptic)")
            }
            previousSteps = newSteps
            currentSteps = newSteps

            if (currentSteps >= targetSteps && !goalAchieved) {
                goalAchieved = true
                hapticManager?.goalAchieved()
                // 튜토리얼 목표 달성 플래그 저장 (걸음수 리셋되어도 유지)
                preferenceManager.saveTutorialGoalAchieved(true)
                // 목표 달성 알림 발송
                notificationHelper.showTutorialGoalAchievedNotification(targetSteps)
                android.util.Log.d("WalkingTest", "🎉 Goal achieved! Flag saved + Notification sent.")
            }
            delay(1000) // 튜토리얼에서는 즉각적 피드백을 위해 1초
        }
    }

    val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)

    val speechText = when {
        goalAchieved -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_walk_done)
            PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_walk_done)
            PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_walk_done)
            PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_walk_done)
            PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_walk_done)
            PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_walk_done)
        }
        currentSteps == 0 -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_walk_zero)
            PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_walk_zero)
            PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_walk_zero)
            PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_walk_zero)
            PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_walk_zero)
            PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_walk_zero)
        }
        else -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_walk)
            PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_walk)
            PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_walk)
            PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_walk)
            PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_walk)
            PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_walk)
        }
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = if (goalAchieved) stringResource(R.string.goal_achieved_exclaim) else stringResource(R.string.walk_steps_format, targetSteps),
        buttonText = if (goalAchieved) stringResource(R.string.next) else stringResource(R.string.steps_needed),
        onButtonClick = {
            if (goalAchieved) {
                hapticManager?.click()
                onNext()
            }
        },
        buttonEnabled = goalAchieved,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots,
        isWalking = !goalAchieved && currentSteps > 0
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 걸음 수 표시
            Text(
                text = "$currentSteps",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = stringResource(R.string.out_of_steps, targetSteps),
                fontSize = 18.sp,
                color = MockupColors.TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 프로그레스 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(MockupColors.Border)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 안내 메시지
            if (!goalAchieved) {
                if (useHealthConnect) {
                    Text(
                        text = stringResource(R.string.health_sync_delay),
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.steps_not_increasing),
                        fontSize = 11.sp,
                        color = MockupColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sync_30_seconds),
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.notification_when_done),
                        fontSize = 11.sp,
                        color = MockupColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.take_light_walk),
                        fontSize = 11.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 걷기 어려울 때 버튼
            if (!goalAchieved) {
                Button(
                    onClick = {
                        manualOffset += 10  // Health Connect 모드에서도 작동
                        // PreferenceManager에도 저장해서 AppBlockService가 목표 달성 인식하도록
                        val newTotal = currentSteps + 10
                        preferenceManager.saveTodaySteps(baselineSteps + newTotal)
                        android.util.Log.d("WalkingTest", "📝 Manual +10 steps saved: total=${baselineSteps + newTotal}")
                        hapticManager?.lightClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cant_walk_now), color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

// =====================================================
// STEP 13: Unlocked (잠금 해제)
// =====================================================
@Composable
private fun HowItWorksStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_howto)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_howto)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_howto)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_howto)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_howto)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_howto)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.rebon_usage),
        buttonText = stringResource(R.string.next),
        onButtonClick = {
            hapticManager?.success()
            onNext()
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 핵심 규칙
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelIcon(iconName = "icon_star", size = 20.dp)
                    Text(
                        text = stringResource(R.string.core_rule),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.goal_rule_text),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 20.sp
                )
            }

            // 15분 휴식
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelIcon(iconName = "icon_timer", size = 20.dp)
                    Text(
                        text = stringResource(R.string.break_mode_15min),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.break_mode_desc),
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// =====================================================
// STEP 14: Emergency Button (긴급 버튼)
// =====================================================
@Composable
private fun EmergencyButtonStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_emergency)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_emergency)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_emergency)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_emergency)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_emergency)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_emergency)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.take_a_break),
        buttonText = stringResource(R.string.next),
        onButtonClick = {
            hapticManager?.success()
            onNext()
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PixelIcon(iconName = "icon_timer", size = 32.dp)

            Text(
                text = stringResource(R.string.break_mode_15min),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Text(
                text = stringResource(R.string.break_mode_details),
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

// =====================================================
// STEP 16: Widget Setup (위젯 설정) - 결제 전 단계
// =====================================================

// 위젯 정보 데이터 클래스
private data class WidgetInfo(
    val name: String,
    val size: String,
    val description: String,
    val icon: String  // Unicode symbol
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetSetupStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onComplete: () -> Unit
) {
    // 기기 언어에 따라 언어 학습 위젯 결정
    val isDeviceKorean = java.util.Locale.getDefault().language == "ko"

    // 위젯 목록 (로컬라이즈 + 언어 기반 필터링)
    val widgetList = listOf(
        WidgetInfo(stringResource(R.string.widget_steps), "2×1", stringResource(R.string.widget_steps_desc), UnicodeSymbols.FOOTPRINTS),
        WidgetInfo(stringResource(R.string.widget_pet), "2×2", stringResource(R.string.widget_pet_desc), UnicodeSymbols.SPARKLES),
        WidgetInfo(stringResource(R.string.widget_weather), "4×1", stringResource(R.string.widget_weather_desc), UnicodeSymbols.SUN),
        WidgetInfo(stringResource(R.string.widget_quote), "2×2", stringResource(R.string.widget_quote_desc), UnicodeSymbols.STAR),
        WidgetInfo(stringResource(R.string.widget_fasting), "2×1", stringResource(R.string.widget_fasting_desc), UnicodeSymbols.CLOCK),
        // 한국어 기기 → 외국어 학습, 외국 기기 → 한국어 학습
        if (isDeviceKorean) {
            WidgetInfo(stringResource(R.string.widget_vocab), "2×1", stringResource(R.string.widget_vocab_desc), UnicodeSymbols.GLOBE)
        } else {
            WidgetInfo(stringResource(R.string.widget_korean), "2×1", stringResource(R.string.widget_korean_desc), UnicodeSymbols.GLOBE)
        },
        WidgetInfo(stringResource(R.string.widget_sudoku), "2×2", stringResource(R.string.widget_sudoku_desc), UnicodeSymbols.GRID)
    )

    val pagerState = rememberPagerState(pageCount = { widgetList.size })
    val currentWidget = widgetList[pagerState.currentPage]

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> stringResource(R.string.speech_loyal_widget)
        PetPersonalityV2.TSUNDERE -> stringResource(R.string.speech_tsundere_widget)
        PetPersonalityV2.FOODIE -> stringResource(R.string.speech_foodie_widget)
        PetPersonalityV2.PLAYFUL -> stringResource(R.string.speech_playful_widget)
        PetPersonalityV2.TIMID -> stringResource(R.string.speech_timid_widget)
        PetPersonalityV2.CLUMSY -> stringResource(R.string.speech_clumsy_widget)
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = stringResource(R.string.seven_widgets),
        buttonText = stringResource(R.string.next),
        onButtonClick = {
            hapticManager?.click()
            onComplete()
        },
        buttonEnabled = true,
        showNavigationDots = true,
        currentDotStep = dotStep,
        totalDotSteps = totalDots
    ) {
        // 현재 위젯 설명
        Text(
            text = currentWidget.description,
            fontSize = 14.sp,
            color = MockupColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 위젯 캐러셀
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentPadding = PaddingValues(horizontal = 50.dp),
            pageSpacing = 12.dp
        ) { page ->
            val widget = widgetList[page]
            WidgetPreviewCard(
                widget = widget,
                petType = petType,
                isCurrentPage = pagerState.currentPage == page
            )
        }
    }
}

// 위젯 미리보기 카드
@Composable
private fun WidgetPreviewCard(
    widget: WidgetInfo,
    petType: PetTypeV2,
    isCurrentPage: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 위젯 미리보기 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            WidgetMockup(widget = widget, petType = petType)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 위젯 이름 + 크기
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = widget.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = widget.size,
                fontSize = 10.sp,
                color = MockupColors.TextMuted,
                modifier = Modifier
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

// 위젯 목업 (각 위젯별 미리보기)
@Composable
private fun WidgetMockup(
    widget: WidgetInfo,
    petType: PetTypeV2
) {
    // Widget names come from stringResource, match by checking contains
    val stepsName = stringResource(R.string.widget_steps)
    val petName = stringResource(R.string.widget_pet)
    val weatherName = stringResource(R.string.widget_weather)
    val quoteName = stringResource(R.string.widget_quote)
    val fastingName = stringResource(R.string.widget_fasting)
    val vocabName = stringResource(R.string.widget_vocab)
    val koreanName = stringResource(R.string.widget_korean)
    val sudokuName = stringResource(R.string.widget_sudoku)

    when (widget.name) {
        stepsName -> StepWidgetMockup(petType)
        petName -> PetWidgetMockup(petType)
        weatherName -> WeatherWidgetMockup(petType)
        quoteName -> QuoteWidgetMockup()
        fastingName -> FastingWidgetMockup()
        vocabName, koreanName -> TravelPhraseWidgetMockup()
        sudokuName -> SudokuWidgetMockup()
    }
}

// 걸음 수 위젯 목업
@Composable
private fun StepWidgetMockup(petType: PetTypeV2) {
    Row(
        modifier = Modifier
            .width(160.dp)
            .height(60.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "5,234",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = PetTutorialStrings.steps(),
                fontSize = 11.sp,
                color = MockupColors.TextMuted
            )
        }
        PetSpriteV2WithGlow(
            petType = petType,
            stage = PetGrowthStage.BABY,
            animationType = PetAnimationTypeV2.IDLE,
            size = 40.dp,
            monochrome = true,
            showGlow = false,
            applyDisplayScale = false
        )
    }
}

// 펫 위젯 목업 (2x2)
@Composable
private fun PetWidgetMockup(petType: PetTypeV2) {
    Column(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = PetTutorialStrings.petWidgetGreeting(),
            fontSize = 10.sp,
            color = MockupColors.TextSecondary,
            modifier = Modifier
                .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        PetSpriteV2WithGlow(
            petType = petType,
            stage = PetGrowthStage.BABY,
            animationType = PetAnimationTypeV2.IDLE,
            size = 50.dp,
            monochrome = true,
            showGlow = false,
            applyDisplayScale = false
        )
    }
}

// 날씨 위젯 목업 (4x1)
@Composable
private fun WeatherWidgetMockup(petType: PetTypeV2) {
    Row(
        modifier = Modifier
            .width(200.dp)
            .height(50.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 시간별 날씨 아이콘들
        listOf("${UnicodeSymbols.SUN}\n18°", "${UnicodeSymbols.CLOUD}\n16°", "${UnicodeSymbols.SUN}\n20°", "${UnicodeSymbols.CLOUD}\n17°").forEach { item ->
            Text(
                text = item,
                fontSize = 9.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
        // 작은 펫
        PetSpriteV2WithGlow(
            petType = petType,
            stage = PetGrowthStage.BABY,
            animationType = PetAnimationTypeV2.IDLE,
            size = 30.dp,
            monochrome = true,
            showGlow = false,
            applyDisplayScale = false
        )
    }
}

// 명언 위젯 목업 (2x2)
@Composable
private fun QuoteWidgetMockup() {
    Column(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\"",
            fontSize = 20.sp,
            color = Color(0xFFCCCCCC)
        )
        Text(
            text = PetTutorialStrings.quoteMockupText(),
            fontSize = 10.sp,
            color = MockupColors.TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

// 단식 타이머 위젯 목업 (2x1)
@Composable
private fun FastingWidgetMockup() {
    Row(
        modifier = Modifier
            .width(160.dp)
            .height(60.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = PetTutorialStrings.fasting(),
                fontSize = 11.sp,
                color = MockupColors.TextMuted
            )
            Text(
                text = "12:34:56",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }
        Text(
            text = UnicodeSymbols.CLOCK,
            fontSize = 24.sp,
            color = MockupColors.TextPrimary
        )
    }
}

// 오늘의 단어 위젯 목업 (2x1) - 일본어 예시
@Composable
private fun TravelPhraseWidgetMockup() {
    Row(
        modifier = Modifier
            .width(160.dp)
            .height(60.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = PetTutorialStrings.thankYouKorean(),
                fontSize = 11.sp,
                color = MockupColors.TextMuted
            )
            Text(
                text = "ありがとう",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
        }
        Text(
            text = "日",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            modifier = Modifier
                .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// 스도쿠 위젯 목업 (2x2)
@Composable
private fun SudokuWidgetMockup() {
    Column(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 3x3 미니 그리드
        Column {
            repeat(3) { row ->
                Row {
                    repeat(3) { col ->
                        val num = ((row * 3 + col + 1) % 9) + 1
                        val isEmpty = (row == 1 && col == 1) || (row == 0 && col == 2)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (isEmpty) Color(0xFFF0F0F0) else Color.White
                                )
                                .border(0.5.dp, Color(0xFFDDDDDD)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isEmpty) {
                                Text(
                                    text = "$num",
                                    fontSize = 10.sp,
                                    color = MockupColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = PetTutorialStrings.tapToSolve(),
            fontSize = 8.sp,
            color = MockupColors.TextMuted
        )
    }
}

// =====================================================
// STEP 15: Payment (결제) - 재결제 화면으로도 사용 가능
// =====================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentScreen(
    petType: PetTypeV2,
    petName: String,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onComplete: () -> Unit,
    petStateV2: PetState? = null  // V2 펫 상태 (있으면 V2 스프라이트 사용)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var billingManager by remember { mutableStateOf<BillingManager?>(null) }
    var promoCode by remember { mutableStateOf("") }
    var showPromoInput by remember { mutableStateOf(false) }
    var promoMessage by remember { mutableStateOf<String?>(null) }
    var isPromoApplied by remember { mutableStateOf(false) }
    var isPromoFree by remember { mutableStateOf(false) }
    var isPromoGuest by remember { mutableStateOf(false) }  // FRIEND_INVITE로 들어온 게스트인지
    val promoCodeManager = remember { PromoCodeManager(context) }

    // 구독 플랜 선택 (월간/연간) - 연간이 기본 선택 (더 이득이므로)
    var selectedPlan by remember { mutableStateOf(BillingManager.SubscriptionType.YEARLY) }

    // 구독 가격 (Google Play에서 조회)
    var monthlyPrice by remember { mutableStateOf<String?>(null) }
    var yearlyPrice by remember { mutableStateOf<String?>(null) }
    var dailyPrice by remember { mutableStateOf<String?>(null) }

    // 가격 조회용 BillingManager (화면 진입 시 즉시 연결)
    var priceBillingManager by remember { mutableStateOf<BillingManager?>(null) }
    DisposableEffect(Unit) {
        val priceBilling = BillingManager(
            context = context,
            onConnectionReady = {
                scope.launch {
                    priceBillingManager?.let { billing ->
                        val prices = billing.getSubscriptionPrices()
                        monthlyPrice = prices.monthlyPrice
                        yearlyPrice = prices.yearlyPrice
                        dailyPrice = prices.dailyPrice
                    }
                }
            }
        )
        priceBillingManager = priceBilling
        onDispose {
            priceBilling.destroy()
        }
    }

    // 입장 애니메이션 상태
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true

        // 딥링크로 받은 프로모 코드 자동 적용
        val pendingCode = preferenceManager.getPendingPromoCode()
        if (!pendingCode.isNullOrBlank()) {
            promoCode = pendingCode
            showPromoInput = true
            when (val result = promoCodeManager.validateAndApply(pendingCode)) {
                is PromoCodeManager.PromoResult.Success -> {
                    promoMessage = result.message
                    isPromoApplied = true
                    isPromoFree = result.freeDays > 0
                    isPromoGuest = result.type == PromoCodeManager.PromoType.FRIEND_INVITE
                }
                is PromoCodeManager.PromoResult.Error -> {
                    promoMessage = result.message
                }
            }
            preferenceManager.clearPendingPromoCode()
        }
    }

    // 펫 슬라이드 인 애니메이션
    val petOffsetX by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 100.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "petSlide"
    )
    val petAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "petAlpha"
    )

    // CTA 버튼 pulse 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 연간 선택 시 하트 이펙트
    var showHeartEffect by remember { mutableStateOf(false) }
    LaunchedEffect(selectedPlan) {
        if (selectedPlan == BillingManager.SubscriptionType.YEARLY) {
            showHeartEffect = true
            kotlinx.coroutines.delay(1000)
            showHeartEffect = false
        }
    }

    val selectedDays = remember { preferenceManager.getControlDays() }
    val selectedPeriods = remember { preferenceManager.getBlockingPeriods() }

    DisposableEffect(Unit) {
        onDispose { billingManager?.destroy() }
    }

    val speechText = when {
        isPromoFree -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> PetTutorialStrings.promoFreeLoyalSpeech()
            PetPersonalityV2.TSUNDERE -> PetTutorialStrings.promoFreeTsundereSpeech()
            PetPersonalityV2.FOODIE -> PetTutorialStrings.promoFreeFoodieSpeech()
            PetPersonalityV2.PLAYFUL -> PetTutorialStrings.promoFreePlayfulSpeech()
            PetPersonalityV2.TIMID -> PetTutorialStrings.promoFreeTimidSpeech()
            PetPersonalityV2.CLUMSY -> PetTutorialStrings.promoFreeClumsySpeech()
        }
        else -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> PetTutorialStrings.paidLoyalSpeech(dailyPrice)
            PetPersonalityV2.TSUNDERE -> PetTutorialStrings.paidTsundereSpeech(dailyPrice)
            PetPersonalityV2.FOODIE -> PetTutorialStrings.paidFoodieSpeech(dailyPrice)
            PetPersonalityV2.PLAYFUL -> PetTutorialStrings.paidPlayfulSpeech(dailyPrice)
            PetPersonalityV2.TIMID -> PetTutorialStrings.paidTimidSpeech(dailyPrice)
            PetPersonalityV2.CLUMSY -> PetTutorialStrings.paidClumsySpeech(dailyPrice)
        }
    }

    // 재결제 유저 판단 (이전에 결제한 적 있으면 재결제)
    val isReturningUser = preferenceManager.isPaidDeposit()

    val buttonText = when {
        isProcessing -> PetTutorialStrings.processing()
        isPromoFree -> PetTutorialStrings.startForFree()
        isReturningUser -> PetTutorialStrings.restartAgain()
        else -> PetTutorialStrings.startForFree()
    }

    // 3일 무료 체험 후 가격 안내 (isYearly 체크)
    val isYearlyPlan = selectedPlan == BillingManager.SubscriptionType.YEARLY
    val trialSubtitle = if (!isPromoFree && !isReturningUser) {
        PetTutorialStrings.trialSubtitle(isYearlyPlan, yearlyPrice, monthlyPrice)
    } else null

    // 결제 처리 함수
    fun processPayment() {
        isProcessing = true
        errorMessage = null

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = java.util.Calendar.getInstance()
        val startDate = sdf.format(today.time)
        today.add(java.util.Calendar.DAY_OF_MONTH, 30)
        val endDate = sdf.format(today.time)

        scope.launch {
            try {
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }

                if (isPromoFree) {
                    preferenceManager.saveDeposit(1)
                    preferenceManager.saveControlStartDate(startDate)
                    preferenceManager.saveControlEndDate(endDate)
                    preferenceManager.saveSuccessDays(0)
                    preferenceManager.setPaidDeposit(false)  // 프로모션 사용자는 결제자가 아님
                    preferenceManager.saveTodaySteps(0)

                    val pastDate = java.util.Calendar.getInstance()
                    pastDate.add(java.util.Calendar.DAY_OF_MONTH, -10)
                    preferenceManager.saveTrialStartDate(sdf.format(pastDate.time))
                    pastDate.add(java.util.Calendar.DAY_OF_MONTH, 3)
                    preferenceManager.saveTrialEndDate(sdf.format(pastDate.time))

                    isProcessing = false
                    hapticManager?.success()
                    onComplete()
                    return@launch
                }

                val activity = context as? android.app.Activity
                if (activity == null) {
                    errorMessage = PetTutorialStrings.activityNotFound()
                    isProcessing = false
                    return@launch
                }

                val subscriptionManager = SubscriptionManager(context)
                billingManager = BillingManager(
                    context = context,
                    onPurchaseSuccess = { purchase ->
                        scope.launch {
                            try {
                                val result = subscriptionManager.createSubscription(
                                    goal = preferenceManager.getGoal(),
                                    controlDays = selectedDays.toList(),
                                    purchase = purchase,
                                    isYearly = selectedPlan == BillingManager.SubscriptionType.YEARLY
                                )
                                if (result.isSuccess) {
                                    // 연간/월간에 따른 가격 저장
                                    val price = if (selectedPlan == BillingManager.SubscriptionType.YEARLY) 39000 else SubscriptionModel.MONTHLY_PRICE
                                    preferenceManager.saveDeposit(price)
                                    preferenceManager.saveControlStartDate(startDate)
                                    preferenceManager.saveControlEndDate(endDate)
                                    preferenceManager.saveSuccessDays(0)
                                    preferenceManager.setPaidDeposit(true)
                                    preferenceManager.saveTodaySteps(0)

                                    val pastDate = java.util.Calendar.getInstance()
                                    pastDate.add(java.util.Calendar.DAY_OF_MONTH, -10)
                                    preferenceManager.saveTrialStartDate(sdf.format(pastDate.time))
                                    pastDate.add(java.util.Calendar.DAY_OF_MONTH, 3)
                                    preferenceManager.saveTrialEndDate(sdf.format(pastDate.time))

                                    isProcessing = false
                                    hapticManager?.success()
                                    onComplete()
                                } else {
                                    errorMessage = PetTutorialStrings.subscriptionSaveFailed()
                                    isProcessing = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "${PetTutorialStrings.errorPrefix()} ${e.message}"
                                isProcessing = false
                            }
                        }
                    },
                    onPurchaseFailure = { error ->
                        errorMessage = error
                        isProcessing = false
                    }
                )
                billingManager?.startSubscription(activity, selectedPlan)

            } catch (e: Exception) {
                errorMessage = "${PetTutorialStrings.errorPrefix()} ${e.message}"
                isProcessing = false
            }
        }
    }

    val kenneyFont = rememberKenneyFont()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        // 상단 컨텐츠 (스크롤 없이 화면에 맞춤)
        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.height(4.dp))

        // 로고 - DEBUG: 길게 누르면 건너뛰기
        Text(
            text = "rebon",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            modifier = if (BuildConfig.DEBUG) {
                Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val today = java.util.Calendar.getInstance()
                        preferenceManager.saveDeposit(10000)
                        preferenceManager.saveControlStartDate(sdf.format(today.time))
                        today.add(java.util.Calendar.DAY_OF_MONTH, 30)
                        preferenceManager.saveControlEndDate(sdf.format(today.time))
                        preferenceManager.saveSuccessDays(0)
                        preferenceManager.setPaidDeposit(true)
                        val pastDate = java.util.Calendar.getInstance()
                        pastDate.add(java.util.Calendar.DAY_OF_MONTH, -10)
                        preferenceManager.saveTrialStartDate(sdf.format(pastDate.time))
                        pastDate.add(java.util.Calendar.DAY_OF_MONTH, 3)
                        preferenceManager.saveTrialEndDate(sdf.format(pastDate.time))
                        preferenceManager.saveTodaySteps(0)
                        hapticManager?.success()
                        onComplete()
                    }
                )
            } else Modifier
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 헤드라인 - 변화 강조
        Text(
            text = PetTutorialStrings.monthAfterChanged(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 펫 영역 (말풍선 + 펫 + 이름) - 슬라이드 인 애니메이션
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .offset(x = petOffsetX)
                    .graphicsLayer { alpha = petAlpha },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Speech bubble
                SpeechBubble(text = speechText, fontSize = 14.sp)
                // 펫 애니메이션
                Box {
                    if (petStateV2 != null) {
                        PetSpriteFromState(
                            petState = petStateV2,
                            isWalking = true,
                            progressPercent = 100,
                            baseSizeDp = 64,
                            monochrome = true
                        )
                    } else {
                        PetSpriteV2WithGlow(
                            petType = petType,
                            stage = PetGrowthStage.BABY,
                            animationType = PetAnimationTypeV2.RUN,
                            size = 64.dp,
                            monochrome = true,
                            showGlow = false,
                            applyDisplayScale = false
                        )
                    }
                    // 연간 선택 시 하트 이펙트
                    if (showHeartEffect) {
                        Text(
                            text = UnicodeSymbols.HEART,
                            fontSize = 20.sp,
                            color = MockupColors.TextPrimary,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-4).dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (isPromoFree) {
            // 프로모션 무료 상태
            Text(
                text = PetTutorialStrings.freeStart(),
                fontSize = 22.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPromoGuest) PetTutorialStrings.oneMonthAllFeaturesFree() else PetTutorialStrings.canInviteOneFriendFree(),
                fontSize = 16.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        } else {
            // 플랜 캐러셀
            val pagerState = rememberPagerState(initialPage = 0) { 2 }

            // 선택된 플랜 동기화
            LaunchedEffect(pagerState.currentPage) {
                selectedPlan = if (pagerState.currentPage == 0)
                    BillingManager.SubscriptionType.YEARLY
                else
                    BillingManager.SubscriptionType.MONTHLY
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp),
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 12.dp
            ) { page ->
                val isYearly = page == 0
                val isSelected = pagerState.currentPage == page

                // 카드 선택 애니메이션
                val cardScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 0.95f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "cardScale"
                )
                val cardElevation by animateDpAsState(
                    targetValue = if (isSelected) 8.dp else 0.dp,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "cardElevation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MockupColors.TextPrimary.copy(alpha = 0.1f)
                            else Color(0xFFF5F5F5)
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                width = 2.dp,
                                color = MockupColors.TextPrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) else Modifier
                        )
                        .clickable {
                            hapticManager?.click()
                            scope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 연간 추천 뱃지
                    if (isYearly) {
                        Text(
                            text = "Popular",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 0.dp)
                                .background(
                                    MockupColors.TextPrimary,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isYearly) "Yearly" else "Monthly",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = if (isYearly) (yearlyPrice ?: "$26.99") else (monthlyPrice ?: "$2.49"),
                                fontSize = 24.sp,
                                fontFamily = kenneyFont,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = if (isYearly) PetTutorialStrings.perYear() else PetTutorialStrings.perMonth(),
                                fontSize = 11.sp,
                                color = MockupColors.TextMuted,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Text(
                            text = PetTutorialStrings.dailyPriceCents(dailyPrice),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MockupColors.TextSecondary
                        )
                    }
                }
            }

            // 인디케이터
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { index ->
                    val dotScale by animateFloatAsState(
                        targetValue = if (pagerState.currentPage == index) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f),
                        label = "dotScale"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) MockupColors.TextPrimary
                                else MockupColors.Border
                            )
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 혜택 리스트 (순차 fade in 애니메이션)
        val benefits = listOf(
            Triple("icon_heart", PetTutorialStrings.benefitAiPetCare(), PetTutorialStrings.benefitAiPetCareDesc()),
            Triple("icon_lock", PetTutorialStrings.benefitSmartBlock(), PetTutorialStrings.benefitSmartBlockDesc()),
            Triple("icon_target", PetTutorialStrings.benefitHomeWidget(), PetTutorialStrings.benefitHomeWidgetDesc()),
            Triple("icon_trophy",
                if (selectedPlan == BillingManager.SubscriptionType.YEARLY) PetTutorialStrings.benefitInvite12Friends() else PetTutorialStrings.benefitInvite1Friend(),
                PetTutorialStrings.benefitInviteDesc())
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            benefits.forEachIndexed { index, (icon, title, description) ->
                // 각 혜택 순차 fade in
                val benefitAlpha by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = 600 + (index * 100),
                        easing = FastOutSlowInEasing
                    ),
                    label = "benefitAlpha$index"
                )
                val benefitOffsetY by animateDpAsState(
                    targetValue = if (isVisible) 0.dp else 20.dp,
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = 600 + (index * 100),
                        easing = FastOutSlowInEasing
                    ),
                    label = "benefitOffset$index"
                )
                Box(
                    modifier = Modifier
                        .graphicsLayer { this.alpha = benefitAlpha }
                        .offset(y = benefitOffsetY)
                ) {
                    BenefitItemLarge(icon = icon, title = title, description = description)
                }
            }
        }

        // 소셜 프루프
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = PetTutorialStrings.socialProof(),
            fontSize = 11.sp,
            color = MockupColors.TextMuted
        )
    }

    // 하단 고정 컨텐츠
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 오류 메시지
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                fontSize = 12.sp,
                color = MockupColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 프로모션 코드 토글 (버튼 위) - 이모지 대신 PixelIcon
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showPromoInput = !showPromoInput }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPromoApplied) {
                    PixelIcon(iconName = "icon_trophy", size = 16.dp)
                } else {
                    PixelIcon(iconName = "icon_star", size = 16.dp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPromoApplied) PetTutorialStrings.applied() else PetTutorialStrings.inviteCode(),
                    fontSize = 14.sp,
                    color = if (isPromoApplied) MockupColors.TextPrimary else MockupColors.TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showPromoInput) "▲" else "▼",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
            }

            if (showPromoInput && !isPromoApplied) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it.uppercase(); promoMessage = null },
                        placeholder = { Text(PetTutorialStrings.enterCode(), fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MockupColors.Border,
                            unfocusedBorderColor = MockupColors.Border
                        )
                    )
                    Button(
                        onClick = {
                            if (promoCode.isNotEmpty()) {
                                promoMessage = PetTutorialStrings.verifying()
                                scope.launch {
                                    when (val result = promoCodeManager.validateAndApply(promoCode)) {
                                        is PromoCodeManager.PromoResult.Success -> {
                                            promoMessage = result.message
                                            isPromoApplied = true
                                            isPromoFree = result.freeDays > 0
                                            isPromoGuest = result.type == PromoCodeManager.PromoType.FRIEND_INVITE
                                            if (result.freeDays > 0) {
                                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                                val cal = java.util.Calendar.getInstance()
                                                cal.add(java.util.Calendar.DAY_OF_MONTH, result.freeDays)
                                                val endDate = sdf.format(cal.time)
                                                preferenceManager.savePromoFreeEndDate(endDate)
                                                // Firebase에 프로모션 정보 동기화
                                                val app = context.applicationContext as WalkorWaitApp
                                                app.userDataRepository.savePromoInfo(
                                                    code = promoCode.uppercase(),
                                                    type = preferenceManager.getPromoCodeType(),
                                                    hostId = preferenceManager.getPromoHostId(),
                                                    endDate = endDate
                                                )
                                            }
                                            hapticManager?.success()
                                        }
                                        is PromoCodeManager.PromoResult.Error -> {
                                            promoMessage = result.message
                                            isPromoApplied = false
                                            isPromoFree = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = promoCode.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Border)
                    ) {
                        Text(PetTutorialStrings.apply(), fontWeight = FontWeight.Bold)
                    }
                }
                if (promoMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = promoMessage ?: "",
                        fontSize = 12.sp,
                        color = if (isPromoApplied) MockupColors.TextPrimary else MockupColors.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Button - pulse 애니메이션 + 결제 버튼
        Button(
            onClick = {
                hapticManager?.success()
                processPayment()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    this.scaleX = if (!isProcessing) pulseScale else 1f
                    this.scaleY = if (!isProcessing) pulseScale else 1f
                },
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = MockupColors.TextPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 3일 무료 체험 후 가격 안내
        if (trialSubtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trialSubtitle,
                fontSize = 12.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            // 슬로건: 10센트로 인생을 바꿔보세요
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = PetTutorialStrings.changYourLifeSlogan(dailyPrice),
                fontSize = 11.sp,
                color = MockupColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
    }
}

// 혜택 아이템 컴포넌트
@Composable
private fun BenefitItem(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PixelIcon(iconName = icon, size = 16.dp)
        Text(
            text = text,
            fontSize = 13.sp,
            color = MockupColors.TextSecondary
        )
    }
}

// 큰 혜택 아이템 컴포넌트 (제목 + 설명)
@Composable
private fun BenefitItemLarge(
    icon: String,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PixelIcon(iconName = icon, size = 24.dp)
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MockupColors.TextSecondary
            )
        }
    }
}

// ===== PREVIEW =====
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PaymentScreenPreview() {
    PaymentScreen(
        petType = PetTypeV2.SHIBA,
        petName = "멍멍이",
        preferenceManager = PreferenceManager(androidx.compose.ui.platform.LocalContext.current),
        hapticManager = null,
        onComplete = {}
    )
}

