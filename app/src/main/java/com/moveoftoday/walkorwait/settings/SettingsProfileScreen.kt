package com.moveoftoday.walkorwait.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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
    val earnedCoupon = SubscriptionModel.earnsFriendCoupon(achievementRate)

    // 결제 상태
    val isPaidDeposit = repository.isPaidDeposit()

    // 프로모션 사용자 체크
    val isPromoFreeUser = preferenceManager?.getPromoCodeType() != null

    // 초대 코드 생성
    val userId = auth.currentUser?.uid ?: ""
    val monthId = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
    val userPart = userId.take(3).uppercase()
    val basicHash = (userId + monthId).hashCode().toString(16).takeLast(4).uppercase()
    val bonusHash = (userId + monthId + "bonus").hashCode().toString(16).takeLast(4).uppercase()
    val basicInviteCode = if (userId.isNotEmpty()) "REBON-$userPart$basicHash" else ""
    val bonusInviteCode = if (userId.isNotEmpty()) "BONUS-$userPart$bonusHash" else ""
    val canShareInviteCode = isPaidDeposit && !isPromoFreeUser && basicInviteCode.isNotEmpty()

    // Guest 정보
    var basicGuestEmail by remember { mutableStateOf<String?>(null) }
    var bonusGuestEmail by remember { mutableStateOf<String?>(null) }

    // Firebase에서 Guest 정보 가져오기
    LaunchedEffect(userId, monthId) {
        if (userId.isNotEmpty() && isPaidDeposit) {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val subDoc = db.collection("users").document(userId)
                    .collection("subscriptions").document(monthId).get().await()
                if (subDoc.exists()) {
                    basicGuestEmail = subDoc.getString("inviteGuestEmail")
                    bonusGuestEmail = subDoc.getString("bonusGuestEmail")
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
            ) {
                // ========== 이번 달 달성률 ==========
                RetroSectionTitle("이번 달 달성률", kenneyFont)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, if (earnedCoupon) MockupColors.Blue else MockupColors.Border, RoundedCornerShape(12.dp))
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
                                text = "달성률",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = "${achievementRate.toInt()}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (earnedCoupon) MockupColors.Blue else MockupColors.TextPrimary,
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
                                    .background(
                                        if (earnedCoupon) MockupColors.Blue else MockupColors.Red,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${successDays}/${totalDays}일 성공",
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                            Text(
                                text = "목표 95%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Blue
                            )
                        }

                        // 쿠폰 혜택 (프로모션 유저 제외)
                        if (!isPromoFreeUser) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (earnedCoupon) MockupColors.BlueLight else MockupColors.CardBackground)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (earnedCoupon) "친구 초대 쿠폰 획득!" else "95% 달성 시 친구 초대 쿠폰!",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (earnedCoupon) MockupColors.Blue else MockupColors.TextPrimary
                                        )
                                        if (earnedCoupon) {
                                            Text(
                                                text = "친구에게 1달 무료 선물하세요",
                                                fontSize = 13.sp,
                                                color = MockupColors.Blue
                                            )
                                        }
                                    }
                                    PixelIcon(
                                        iconName = if (earnedCoupon) "icon_trophy" else "icon_chest",
                                        size = 32.dp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 친구 초대 ==========
                if (canShareInviteCode) {
                    RetroSectionTitle("친구 초대", kenneyFont)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, MockupColors.Blue, RoundedCornerShape(12.dp))
                            .background(MockupColors.BlueLight, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "친구에게 1달 무료 쿠폰을 선물하세요",
                                fontSize = 14.sp,
                                color = MockupColors.TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 기본 초대 코드
                            InviteCodeBox(
                                label = "기본 초대 코드",
                                code = basicInviteCode,
                                guestEmail = basicGuestEmail,
                                isActive = true,
                                color = MockupColors.Blue,
                                kenneyFont = kenneyFont,
                                context = context,
                                hapticManager = hapticManager
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 보너스 초대 코드
                            InviteCodeBox(
                                label = "보너스 초대 코드 ${if (earnedCoupon) "" else "(95% 달성 시)"}",
                                code = bonusInviteCode,
                                guestEmail = bonusGuestEmail,
                                isActive = earnedCoupon,
                                color = MockupColors.Purple,
                                kenneyFont = kenneyFont,
                                context = context,
                                hapticManager = hapticManager
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ========== 계정 ==========
                RetroSectionTitle("계정", kenneyFont)

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
                                        text = "Google 연결됨",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Green
                                    )
                                    Text(
                                        text = googleEmail,
                                        fontSize = 13.sp,
                                        color = MockupColors.TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MockupColors.Green,
                                    modifier = Modifier.size(24.dp)
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
                                        Toast.makeText(context, "로그아웃 완료", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "로그아웃",
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
                                text = "Google 계정을 연결하면\n다른 기기에서 데이터를 복원할 수 있어요",
                                fontSize = 14.sp,
                                color = MockupColors.TextSecondary,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MockupColors.Blue, RoundedCornerShape(8.dp))
                                    .background(MockupColors.BlueLight, RoundedCornerShape(8.dp))
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
                                                        Toast.makeText(context, "Google 계정 연결 완료!", Toast.LENGTH_SHORT).show()
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
                                        color = MockupColors.Blue
                                    )
                                } else {
                                    Text(
                                        text = "Google로 로그인",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupColors.Blue
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 초대 코드 박스
 */
@Composable
private fun InviteCodeBox(
    label: String,
    code: String,
    guestEmail: String?,
    isActive: Boolean,
    color: Color,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    context: Context,
    hapticManager: HapticManager
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = if (isActive) color else MockupColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MockupColors.TextPrimary else MockupColors.TextMuted
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    if (guestEmail != null) MockupColors.Green else if (isActive) color else MockupColors.Border,
                    RoundedCornerShape(8.dp)
                )
                .background(
                    if (guestEmail != null) MockupColors.GreenLight else if (isActive) color.copy(alpha = 0.1f) else MockupColors.Background.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            if (guestEmail != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MockupColors.Green,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${guestEmail.substringBefore("@")}님이 사용 중",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.Green
                    )
                }
            } else if (isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = code,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontFamily = kenneyFont
                    )
                    Box(
                        modifier = Modifier
                            .border(2.dp, color, RoundedCornerShape(6.dp))
                            .background(MockupColors.CardBackground, RoundedCornerShape(6.dp))
                            .clickable {
                                hapticManager.success()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("invite_code", code)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "복사 완료!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "복사",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontFamily = kenneyFont
                        )
                    }
                }
            } else {
                Text(
                    text = "🔒 ${code.take(10)}...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextMuted,
                    fontFamily = kenneyFont
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
