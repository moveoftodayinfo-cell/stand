package com.moveoftoday.walkorwait.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsProfileStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun thisMonthAchievement(): String = when (getLang()) {
        "ko" -> "이번 달 달성률"
        "ja" -> "今月の達成率"
        "zh" -> "本月达成率"
        "es" -> "Logro este mes"
        else -> "This Month Achievement"
    }

    fun achievementRate(): String = when (getLang()) {
        "ko" -> "달성률"
        "ja" -> "達成率"
        "zh" -> "达成率"
        "es" -> "Tasa de logro"
        else -> "Achievement Rate"
    }

    fun successDaysFormat(success: Int, total: Int): String = when (getLang()) {
        "ko" -> "${success}/${total}일 성공"
        "ja" -> "${success}/${total}日成功"
        "zh" -> "${success}/${total}天成功"
        "es" -> "${success}/${total} días exitosos"
        else -> "${success}/${total} days success"
    }

    fun streakDefenseTicket(): String = when (getLang()) {
        "ko" -> "streak 방어 티켓"
        "ja" -> "ストリーク防御チケット"
        "zh" -> "连续达成保护券"
        "es" -> "Ticket de defensa de racha"
        else -> "Streak Defense Ticket"
    }

    fun ticketCount(count: Int): String = when (getLang()) {
        "ko" -> "${count}장"
        "ja" -> "${count}枚"
        "zh" -> "${count}张"
        "es" -> "$count tickets"
        else -> "$count tickets"
    }

    fun keepStreakOnFail(): String = when (getLang()) {
        "ko" -> "하루 실패해도 streak 유지!"
        "ja" -> "1日失敗してもストリーク維持！"
        "zh" -> "失败一天也能保持连续达成！"
        "es" -> "¡Mantén tu racha aunque falles un día!"
        else -> "Keep streak even if you fail a day!"
    }

    fun earnAt90Percent(): String = when (getLang()) {
        "ko" -> "90% 달성 시 획득"
        "ja" -> "90%達成で獲得"
        "zh" -> "达成90%时获得"
        "es" -> "Se obtiene al lograr 90%"
        else -> "Earned at 90% achievement"
    }

    fun inviteFriend(): String = when (getLang()) {
        "ko" -> "친구 초대"
        "ja" -> "友達を招待"
        "zh" -> "邀请朋友"
        "es" -> "Invitar amigo"
        else -> "Invite Friend"
    }

    fun freeMonthForFriend(): String = when (getLang()) {
        "ko" -> "친구에게 1달 무료 선물"
        "ja" -> "友達に1ヶ月無料をプレゼント"
        "zh" -> "送朋友1个月免费"
        "es" -> "Regala 1 mes gratis a un amigo"
        else -> "Gift 1 month free to friend"
    }

    fun codeExpiry(date: String): String = when (getLang()) {
        "ko" -> "코드 만료: $date"
        "ja" -> "コード有効期限: $date"
        "zh" -> "代码到期: $date"
        "es" -> "Código expira: $date"
        else -> "Code expires: $date"
    }

    fun remainingInvites(remaining: Int, max: Int): String = when (getLang()) {
        "ko" -> "남은 초대: $remaining/${max}명"
        "ja" -> "残り招待: $remaining/${max}人"
        "zh" -> "剩余邀请: $remaining/${max}人"
        "es" -> "Invitaciones restantes: $remaining/$max"
        else -> "Remaining invites: $remaining/$max"
    }

    fun invitedFriends(): String = when (getLang()) {
        "ko" -> "초대된 친구"
        "ja" -> "招待済みの友達"
        "zh" -> "已邀请的朋友"
        "es" -> "Amigos invitados"
        else -> "Invited Friends"
    }

    fun account(): String = when (getLang()) {
        "ko" -> "계정"
        "ja" -> "アカウント"
        "zh" -> "账户"
        "es" -> "Cuenta"
        else -> "Account"
    }

    fun googleConnected(): String = when (getLang()) {
        "ko" -> "Google 연결됨"
        "ja" -> "Google連携済み"
        "zh" -> "已连接Google"
        "es" -> "Google conectado"
        else -> "Google Connected"
    }

    fun logout(): String = when (getLang()) {
        "ko" -> "로그아웃"
        "ja" -> "ログアウト"
        "zh" -> "退出登录"
        "es" -> "Cerrar sesión"
        else -> "Logout"
    }

    fun logoutComplete(): String = when (getLang()) {
        "ko" -> "로그아웃 완료"
        "ja" -> "ログアウト完了"
        "zh" -> "已退出登录"
        "es" -> "Sesión cerrada"
        else -> "Logged out"
    }

    fun googleAccountBackupInfo(): String = when (getLang()) {
        "ko" -> "Google 계정을 연결하면\n다른 기기에서 데이터를 복원할 수 있어요"
        "ja" -> "Googleアカウントを連携すると\n他のデバイスでデータを復元できます"
        "zh" -> "连接Google账号后\n可在其他设备恢复数据"
        "es" -> "Conecta tu cuenta de Google\npara restaurar datos en otros dispositivos"
        else -> "Connect your Google account\nto restore data on other devices"
    }

    fun loginWithGoogle(): String = when (getLang()) {
        "ko" -> "Google로 로그인"
        "ja" -> "Googleでログイン"
        "zh" -> "使用Google登录"
        "es" -> "Iniciar sesión con Google"
        else -> "Sign in with Google"
    }

    fun googleAccountConnected(): String = when (getLang()) {
        "ko" -> "Google 계정 연결 완료!"
        "ja" -> "Googleアカウント連携完了！"
        "zh" -> "Google账号连接成功！"
        "es" -> "¡Cuenta Google conectada!"
        else -> "Google account connected!"
    }

    fun copied(): String = when (getLang()) {
        "ko" -> "복사 완료!"
        "ja" -> "コピー完了！"
        "zh" -> "复制成功！"
        "es" -> "¡Copiado!"
        else -> "Copied!"
    }

    fun copy(): String = when (getLang()) {
        "ko" -> "복사"
        "ja" -> "コピー"
        "zh" -> "复制"
        "es" -> "Copiar"
        else -> "Copy"
    }

    fun share(): String = when (getLang()) {
        "ko" -> "공유"
        "ja" -> "共有"
        "zh" -> "分享"
        "es" -> "Compartir"
        else -> "Share"
    }

    fun inviteComplete(): String = when (getLang()) {
        "ko" -> "초대 완료"
        "ja" -> "招待完了"
        "zh" -> "邀请完成"
        "es" -> "Invitación completa"
        else -> "Invite Complete"
    }

    fun shareInviteCode(): String = when (getLang()) {
        "ko" -> "초대 코드 공유"
        "ja" -> "招待コードを共有"
        "zh" -> "分享邀请码"
        "es" -> "Compartir código de invitación"
        else -> "Share invite code"
    }

    fun languageSetting(): String = when (getLang()) {
        "ko" -> "언어"
        "ja" -> "言語"
        "zh" -> "语言"
        "es" -> "Idioma"
        else -> "Language"
    }

    fun languageSystem(): String = when (getLang()) {
        "ko" -> "시스템 기본값"
        "ja" -> "システムデフォルト"
        "zh" -> "系统默认"
        "es" -> "Predeterminado del sistema"
        else -> "System Default"
    }

    fun languageChangeRestart(): String = when (getLang()) {
        "ko" -> "언어가 변경되었습니다. 완전히 적용하려면 앱을 다시 시작하세요."
        "ja" -> "言語が変更されました。完全に適用するにはアプリを再起動してください。"
        "zh" -> "语言已更改。请重启应用以完全生效。"
        "es" -> "Idioma cambiado. Reinicia la app para aplicar completamente."
        else -> "Language changed. Please restart the app for full effect."
    }

    fun currentLanguage(lang: String): String = when (getLang()) {
        "ko" -> "현재: $lang"
        "ja" -> "現在: $lang"
        "zh" -> "当前: $lang"
        "es" -> "Actual: $lang"
        else -> "Current: $lang"
    }

    fun getLanguageDisplayName(code: String): String = when (code) {
        "system" -> languageSystem()
        "ko" -> "한국어"
        "ja" -> "日本語"
        "zh" -> "中文 (简体)"
        "es" -> "Español"
        else -> "English"
    }

    fun close(): String = when (getLang()) {
        "ko" -> "닫기"
        "ja" -> "閉じる"
        "zh" -> "关闭"
        "es" -> "Cerrar"
        else -> "Close"
    }

    fun shareMessage(code: String): String = when (getLang()) {
        "ko" -> """
            |${UnicodeSymbols.FOOTPRINTS} rebon - 걸어서 앱을 해제하세요!
            |
            |친구가 rebon을 추천했어요.
            |아래 링크로 가입하면 30일 무료!
            |
            |https://stand-64c11.web.app/invite?code=$code
        """.trimMargin()
        "ja" -> """
            |${UnicodeSymbols.FOOTPRINTS} rebon - 歩いてアプリをアンロック！
            |
            |友達がrebonをおすすめしています。
            |下のリンクから登録で30日無料！
            |
            |https://stand-64c11.web.app/invite?code=$code
        """.trimMargin()
        "zh" -> """
            |${UnicodeSymbols.FOOTPRINTS} rebon - 走路解锁应用！
            |
            |朋友推荐了rebon。
            |通过下面的链接注册即可免费使用30天！
            |
            |https://stand-64c11.web.app/invite?code=$code
        """.trimMargin()
        "es" -> """
            |${UnicodeSymbols.FOOTPRINTS} rebon - ¡Desbloquea apps caminando!
            |
            |Tu amigo te recomienda rebon.
            |¡Regístrate con el enlace y obtén 30 días gratis!
            |
            |https://stand-64c11.web.app/invite?code=$code
        """.trimMargin()
        else -> """
            |${UnicodeSymbols.FOOTPRINTS} rebon - Unlock apps by walking!
            |
            |Your friend recommends rebon.
            |Sign up with the link below for 30 days free!
            |
            |https://stand-64c11.web.app/invite?code=$code
        """.trimMargin()
    }
}

/**
 * 내 정보 화면 (프로필, 구독, 계정)
 */
@Composable
fun SettingsProfileScreen(
    preferenceManager: PreferenceManager?,
    repository: UserDataRepository,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val scope = rememberCoroutineScope()

    // Google 로그인 상태
    val auth = remember { FirebaseAuth.getInstance() }
    var isGoogleSignedIn by remember { mutableStateOf(auth.currentUser != null && auth.currentUser?.isAnonymous != true) }
    var googleEmail by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // 달성률 계산
    val successDays = repository.getSuccessDays()
    val totalDays = preferenceManager?.getTotalControlDays() ?: 0
    val achievementRate = if (totalDays > 0) (successDays.toFloat() / totalDays * 100) else 0f

    // 결제 상태
    val isPaidDeposit = repository.isPaidDeposit()

    // 프로모션 사용자 체크
    val isPromoFreeUser = preferenceManager?.getPromoCodeType() != null

    // streak 방어 티켓 수
    val defenseTickets = preferenceManager?.getStreakDefenseTickets() ?: 0

    // 초대 코드 생성
    val userId = auth.currentUser?.uid ?: ""
    val monthId = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
    val userPart = userId.take(3).uppercase()
    val basicHash = (userId + monthId).hashCode().toString(16).takeLast(4).uppercase()
    val basicInviteCode = if (userId.isNotEmpty()) "REBON-$userPart$basicHash" else ""
    // 친구 초대 코드 공유 조건:
    // 1. 결제자 (isPaidDeposit)
    // 2. 프로모 무료 사용자 아님
    // 3. 첫 결제일로부터 3일 경과 (canShareInviteCodeByDate)
    val canShareByDate = preferenceManager?.canShareInviteCodeByDate() ?: false
    val canShareInviteCode = isPaidDeposit && !isPromoFreeUser && canShareByDate && basicInviteCode.isNotEmpty()

    // 초대 정보
    var maxInvites by remember { mutableStateOf(1) }
    var inviteGuests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var inviteCodeExpiry by remember { mutableStateOf("") }

    // Firebase에서 초대 정보 가져오기
    LaunchedEffect(userId, monthId) {
        if (userId.isNotEmpty() && isPaidDeposit) {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val subDoc = db.collection("users").document(userId)
                    .collection("subscriptions").document(monthId).get().await()
                if (subDoc.exists()) {
                    maxInvites = subDoc.getLong("maxInvites")?.toInt() ?: 1
                    @Suppress("UNCHECKED_CAST")
                    inviteGuests = subDoc.get("inviteGuests") as? List<Map<String, Any>> ?: emptyList()
                    // 초대 코드 만료일 (endDate)
                    val endDate = subDoc.getDate("endDate")
                    if (endDate != null) {
                        val sdf = java.text.SimpleDateFormat("M월 d일", java.util.Locale.KOREA)
                        inviteCodeExpiry = sdf.format(endDate)
                    }
                }
            } catch (e: Exception) {
                // 무시
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            SettingsHeader(
                title = "profile",
                kenneyFont = kenneyFont,
                onBack = {
                    hapticManager.click()
                    onBack()
                }
            )

            // 스크롤 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                // ========== 이번 달 달성률 ==========
                RetroSectionTitle(SettingsProfileStrings.thisMonthAchievement(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SettingsProfileStrings.achievementRate(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = "${achievementRate.toInt()}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary,
                                fontFamily = kenneyFont
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 프로그레스 바
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(4.dp))
                                .background(MockupColors.Background, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(achievementRate / 100f)
                                    .background(MockupColors.TextPrimary, RoundedCornerShape(2.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = SettingsProfileStrings.successDaysFormat(successDays, totalDays),
                            fontSize = 13.sp,
                            color = MockupColors.TextSecondary
                        )

                        // streak 방어 티켓 (프로모션 유저 제외)
                        if (!isPromoFreeUser) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (defenseTickets > 0) MockupColors.Border.copy(alpha = 0.15f) else MockupColors.CardBackground)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = SettingsProfileStrings.streakDefenseTicket(),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupColors.TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // 티켓 수 배지
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (defenseTickets > 0) MockupColors.TextPrimary else MockupColors.TextMuted,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = SettingsProfileStrings.ticketCount(defenseTickets),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontFamily = kenneyFont
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (defenseTickets > 0) SettingsProfileStrings.keepStreakOnFail() else SettingsProfileStrings.earnAt90Percent(),
                                            fontSize = 13.sp,
                                            color = if (defenseTickets > 0) MockupColors.TextPrimary else MockupColors.TextMuted
                                        )
                                    }
                                    PixelIcon(
                                        iconName = "icon_shield",
                                        size = 32.dp,
                                        tint = if (defenseTickets > 0) MockupColors.TextPrimary else MockupColors.TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 친구 초대 ==========
                if (canShareInviteCode) {
                    val remainingInvites = maxInvites - inviteGuests.size

                    RetroSectionTitle(SettingsProfileStrings.inviteFriend(), kenneyFont)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(MockupColors.Border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // 헤더 (설명 + 남은 횟수)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = SettingsProfileStrings.freeMonthForFriend(),
                                        fontSize = 14.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                    if (inviteCodeExpiry.isNotEmpty()) {
                                        Text(
                                            text = SettingsProfileStrings.codeExpiry(inviteCodeExpiry),
                                            fontSize = 12.sp,
                                            color = MockupColors.TextMuted
                                        )
                                    }
                                }
                                Text(
                                    text = SettingsProfileStrings.remainingInvites(remainingInvites, maxInvites),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingInvites > 0) MockupColors.TextPrimary else MockupColors.TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 초대 코드
                            InviteCodeBox(
                                code = basicInviteCode,
                                remainingInvites = remainingInvites,
                                color = MockupColors.TextPrimary,
                                kenneyFont = kenneyFont,
                                context = context,
                                hapticManager = hapticManager
                            )

                            // 게스트 목록
                            if (inviteGuests.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = SettingsProfileStrings.invitedFriends(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                inviteGuests.forEach { guest ->
                                    val email = guest["email"] as? String ?: ""
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = UnicodeSymbols.CHECK,
                                            fontSize = 14.sp,
                                            color = MockupColors.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${email.substringBefore("@")}님",
                                            fontSize = 13.sp,
                                            color = MockupColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ========== 계정 ==========
                RetroSectionTitle(SettingsProfileStrings.account(), kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    if (isGoogleSignedIn) {
                        // 로그인 상태
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = SettingsProfileStrings.googleConnected(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary
                                    )
                                    Text(
                                        text = googleEmail,
                                        fontSize = 13.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                }
                                Text(
                                    text = UnicodeSymbols.CHECK,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 로그아웃 버튼
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MockupColors.Red, RoundedCornerShape(8.dp))
                                    .clickable {
                                        hapticManager.click()
                                        auth.signOut()
                                        isGoogleSignedIn = false
                                        googleEmail = ""
                                        Toast.makeText(context, SettingsProfileStrings.logoutComplete(), Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = SettingsProfileStrings.logout(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupColors.Red
                                )
                            }
                        }
                    } else {
                        // 로그인 버튼
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = SettingsProfileStrings.googleAccountBackupInfo(),
                                fontSize = 14.sp,
                                color = MockupColors.TextSecondary,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                                    .background(MockupColors.Border.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isGoogleLoading) {
                                        hapticManager.click()
                                        isGoogleLoading = true
                                        scope.launch {
                                            val result = GoogleSignInHelper.signIn(context)
                                            when (result) {
                                                is GoogleSignInHelper.SignInResult.Success -> {
                                                    val firebaseResult = GoogleSignInHelper.signInToFirebase(result.idToken)
                                                    if (firebaseResult.isSuccess) {
                                                        repository.startSync()
                                                        isGoogleSignedIn = true
                                                        googleEmail = auth.currentUser?.email ?: ""
                                                        hapticManager.success()
                                                        Toast.makeText(context, SettingsProfileStrings.googleAccountConnected(), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                is GoogleSignInHelper.SignInResult.Error -> {
                                                    if (!result.isCancelled) {
                                                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            isGoogleLoading = false
                                        }
                                    }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGoogleLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MockupColors.TextPrimary
                                    )
                                } else {
                                    Text(
                                        text = SettingsProfileStrings.loginWithGoogle(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ========== 언어 설정 ==========
                RetroSectionTitle(SettingsProfileStrings.languageSetting(), kenneyFont)

                LanguageSettingBox(
                    preferenceManager = preferenceManager,
                    hapticManager = hapticManager,
                    context = context
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 언어 설정 박스
 */
@Composable
private fun LanguageSettingBox(
    preferenceManager: PreferenceManager?,
    hapticManager: HapticManager,
    context: Context
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember {
        mutableStateOf(preferenceManager?.getAppLanguage() ?: "system")
    }

    val languages = listOf(
        "system" to SettingsProfileStrings.languageSystem(),
        "en" to "English",
        "ko" to "한국어",
        "ja" to "日本語",
        "zh" to "中文 (简体)",
        "es" to "Español"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable {
                hapticManager.click()
                showLanguageDialog = true
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = SettingsProfileStrings.languageSetting(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = SettingsProfileStrings.currentLanguage(
                        SettingsProfileStrings.getLanguageDisplayName(currentLanguage)
                    ),
                    fontSize = 13.sp,
                    color = MockupColors.TextSecondary
                )
            }
            Text(
                text = UnicodeSymbols.RIGHT_ARROW,
                fontSize = 18.sp,
                color = MockupColors.TextMuted
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = MockupColors.CardBackground,
            title = {
                Text(
                    text = SettingsProfileStrings.languageSetting(),
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            },
            text = {
                Column {
                    languages.forEach { (code, displayName) ->
                        val isSelected = code == currentLanguage
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MockupColors.Blue else MockupColors.Border,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) MockupColors.Blue.copy(alpha = 0.1f)
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    hapticManager.click()
                                    currentLanguage = code
                                    preferenceManager?.saveAppLanguage(code)

                                    // Apply language change
                                    val localeList = if (code == "system") {
                                        LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        LocaleListCompat.forLanguageTags(code)
                                    }
                                    AppCompatDelegate.setApplicationLocales(localeList)

                                    showLanguageDialog = false
                                    Toast.makeText(
                                        context,
                                        SettingsProfileStrings.languageChangeRestart(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MockupColors.Blue else MockupColors.TextPrimary
                                )
                                if (isSelected) {
                                    Text(
                                        text = UnicodeSymbols.CHECK,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Blue
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(
                        text = SettingsProfileStrings.close(),
                        color = MockupColors.TextSecondary
                    )
                }
            }
        )
    }
}

/**
 * 초대 코드 박스
 */
@Composable
private fun InviteCodeBox(
    code: String,
    remainingInvites: Int,
    color: Color,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    context: Context,
    hapticManager: HapticManager
) {
    val isActive = remainingInvites > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                if (isActive) color else MockupColors.TextMuted,
                RoundedCornerShape(8.dp)
            )
            .background(
                if (isActive) color.copy(alpha = 0.1f) else MockupColors.Background.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = code,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) color else MockupColors.TextMuted,
                fontFamily = kenneyFont
            )
            if (isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 복사 버튼
                    Box(
                        modifier = Modifier
                            .border(2.dp, color, RoundedCornerShape(6.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(6.dp))
                            .clickable {
                                hapticManager.success()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("invite_code", code)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, SettingsProfileStrings.copied(), Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = SettingsProfileStrings.copy(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontFamily = kenneyFont
                        )
                    }
                    // 공유 버튼
                    Box(
                        modifier = Modifier
                            .border(2.dp, color, RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable {
                                hapticManager.success()
                                val shareText = SettingsProfileStrings.shareMessage(code)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, SettingsProfileStrings.shareInviteCode()))
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = SettingsProfileStrings.share(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontFamily = kenneyFont
                        )
                    }
                }
            } else {
                Text(
                    text = SettingsProfileStrings.inviteComplete(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextMuted
                )
            }
        }
    }
}

/**
 * 섹션 타이틀 (재사용)
 */
@Composable
fun RetroSectionTitle(
    title: String,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MockupColors.TextPrimary,
        fontFamily = fontFamily,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}
