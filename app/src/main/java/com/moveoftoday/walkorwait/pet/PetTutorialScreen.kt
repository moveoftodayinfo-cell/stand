package com.moveoftoday.walkorwait.pet

import com.moveoftoday.walkorwait.UnicodeSymbols
import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.app.Activity
import android.util.Log

/**
 * Complete Pet Onboarding Flow - 17 Steps:
 *
 * NO DOTS (0-3):
 * 0. Google Sign-In (데이터 백업) - 기존 데이터 있으면 메인으로 스킵
 * 1. Pet Selection
 * 2. Pet Name Input
 * 3. Tutorial All-in-One (함께 할 것 설명)
 *
 * WITH DOTS (4-15, 12 dots total):
 * 4. Permission Settings (권한 설정) - dot 0
 * 5. Fitness App Connection (피트니스 연결) - dot 1
 * 6. Accessibility (접근성 권한) - dot 2
 * 7. App Selection (앱 선택) - dot 3
 * 8. Test Blocking (차단 테스트) - dot 4
 * 9. Goal Input (목표 입력) - dot 5
 * 10. Walking Test (걷기 테스트) - dot 6
 * 11. Unlocked (잠금 해제) - dot 7
 * 12. Emergency Button (긴급 버튼) - dot 8
 * 13. Control Days (제어 요일) - dot 9
 * 14. Block Time (차단 시간대) - dot 10
 * 15. Payment (결제) - dot 11
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

    // 네비게이션 닷 계산 (Step 4-15는 닷 표시, 12개) - Step 3은 튜토리얼 + 구글 로그인
    val showDots = currentStep in 4..15
    val dotStep = if (showDots) currentStep - 4 else 0
    val totalDots = 12

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
            2 -> PetNameInputStep(
                petType = selectedPetType!!,
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
            3 -> TutorialAllInOneStep(
                petType = selectedPetType!!,
                petName = petName,
                hapticManager = hapticManager,
                onNext = {
                    currentStep = 4
                }
            )

            // === WITH DOTS (4-15) ===
            4 -> PermissionSettingsStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 6  // FitnessConnectionStep(5) 스킵 - 기본 센서 사용
                }
            )
            // Step 5 (FitnessConnectionStep) 제거됨 - 기본 센서를 디폴트로 사용
            // 피트니스 앱 연결은 설정 > 앱 제어에서 선택적으로 가능
            6 -> AccessibilityStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 7
                }
            )
            7 -> AppSelectionStep(
                petType = selectedPetType!!,
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
            8 -> TestBlockingStep(
                petType = selectedPetType!!,
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
            9 -> GoalInputStep(
                petType = selectedPetType!!,
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
            10 -> WalkingTestStep(
                petType = selectedPetType!!,
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
            11 -> UnlockedStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 12
                }
            )
            12 -> EmergencyButtonStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 13
                }
            )
            13 -> ControlDaysStep(
                petType = selectedPetType!!,
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
            14 -> BlockTimeStep(
                petType = selectedPetType!!,
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
            15 -> PaymentScreen(
                petType = selectedPetType!!,
                petName = petName,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onComplete = {
                    hapticManager?.click()
                    currentStep = 16
                }
            )

            // === NO DOTS (16) ===
            16 -> WidgetSetupStep(
                petType = selectedPetType!!,
                petName = petName,
                hapticManager = hapticManager,
                onComplete = {
                    hapticManager?.success()
                    // 모든 튜토리얼 단계 완료 플래그 설정
                    prefManager.setPermissionSetupCompleted(true)
                    prefManager.setHealthConnectSetupCompleted(true)
                    prefManager.setAccessibilitySetupCompleted(true)
                    prefManager.setAppSelectionCompleted(true)
                    prefManager.setTutorialCompleted(true)
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
                        petType = selectedPetType!!.name,
                        petName = petName
                    )

                    // Analytics: 튜토리얼 완료 추적
                    AnalyticsManager.trackTutorialComplete()
                    AnalyticsManager.setUserPetType(selectedPetType!!.name)

                    onComplete(selectedPetType!!, petName)
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
            text = "친구를 골라주세요!",
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
                text = "*친구특징*",
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
            text = "이 친구로!",
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
        PetType.DOG1 -> "듬직하고 멋있는 상남자 스타일\n말수는 적지만 행동으로 보여주는 타입\n묵묵히 당신 곁을 지켜줄 거예요"
        PetType.DOG2 -> "갓생러 지망 강아지\nㄹㅇ 응원이 특기ㅋㅋ\n같이 있으면 텐션 업 보장"
        PetType.CAT1 -> "겉은 차갑지만 속은 따뜻한 츤데레\n관심 없는 척하지만 사실 다 챙겨요\n은근히 당신 걱정을 많이 해요"
        PetType.CAT2 -> "쿨한 부산 고양이\n담백하고 솔직한 말투가 매력\n옆에서 든든하게 챙겨줄 거예요"
        PetType.RAT -> "소심하지만 마음은 따뜻해요\n조심스럽게 당신에게 다가가요\n천천히 친해지면 든든한 친구가 돼요"
        PetType.BIRD -> "언제나 밝고 긍정적인 에너지\n힘들 때 용기를 북돋아 줘요\n함께라면 매일이 즐거워요"
    }
}

/**
 * 펫 특징 설명 (3줄) - V2 새 펫들
 */
private fun getPetDescriptionV2(petType: PetTypeV2): String {
    return when (petType) {
        PetTypeV2.SHIBA -> "충성스럽고 씩씩한 시바견\n조금 고집 세지만 정은 많아요\n당신과 함께라면 어디든 갈 준비 됐어요"
        PetTypeV2.CAT -> "도도하지만 은근 살갑게 다가와요\n자기만의 매력이 철철 넘쳐요\n츤데레? 네, 맞아요 그게 저예요"
        PetTypeV2.PIG -> "먹는 걸 좋아하는 복돼지\n느긋하지만 의외로 똑똑해요\n함께 있으면 행운이 따라올 거예요"
        PetTypeV2.RACCOON -> "호기심 많고 장난기 넘치는 친구\n귀여운 눈망울에 속지 마세요\n엉뚱하지만 당신 곁을 지켜줄 거예요"
        PetTypeV2.HAMSTER -> "작지만 용감한 햄스터\n볼에 가득 채운 건 당신을 향한 마음\n포동포동 귀여움으로 응원할게요"
        PetTypeV2.PENGUIN -> "느긋하고 여유로운 펭귄\n뒤뚱뒤뚱 걸어도 마음은 빨라요\n시원한 친구와 함께 힘내봐요"
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
    val speechText = "내 이름 지어줘."
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
            text = "이름을 지어주세요!",
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
                        text = "8글자 이내",
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
            text = "좋아, 가자!",
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
        PetPersonalityV2.LOYAL -> "준비됐어. 시작하자."
        PetPersonalityV2.TSUNDERE -> "뭐, 잘 부탁해."
        PetPersonalityV2.FOODIE -> "같이 가보자고! ㄱㄱ~"
        PetPersonalityV2.PLAYFUL -> "자 시작하자"
        PetPersonalityV2.TIMID -> "잘, 잘 부탁드려요..."
        PetPersonalityV2.CLUMSY -> "우리 함께 화이팅!"
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
            text = "${petName}와 함께 할 것",
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
                title = "목표 걸음 수 설정",
                description = "매일 달성할 걸음 수 목표를 정해요"
            )
            TutorialItemRow(
                iconName = "icon_boots",
                title = "함께 목표 달성",
                description = "펫이 당신의 걷기를 응원해요"
            )
            TutorialItemRow(
                iconName = "icon_lock",
                title = "앱 사용 제어",
                description = "시간 낭비하는 앱 사용을 줄여줘요"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 시작하기 버튼만 (Google 로그인은 step 0에서 완료됨)
        MockupButton(
            text = "시작하기!",
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
        statusMessage = "로그인 중..."
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

                        statusMessage = "데이터 확인 중..."

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
                            // tutorialCompleted가 false면 true로 수정
                            if (!tutorialCompleted) {
                                prefManager.setTutorialCompleted(true)
                                app.userDataRepository.setTutorialCompleted(true)
                                Log.d("GoogleSignIn", "Fixed tutorialCompleted to true")
                            }
                            // 기존 데이터가 있으면 바로 메인으로
                            statusMessage = "데이터 복원 완료!"
                            delay(1000)
                            onDataRestored()
                        } else {
                            // 기존 데이터 없으면 펫 선택으로
                            statusMessage = "로그인 완료!"
                            delay(500)
                            onNext()
                        }
                    } else {
                        errorMessage = "Firebase 로그인 실패"
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
                    append("동물 친구")
                }
                append("와 걸으면서\n고치는 디지털 습관")
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
            text = "걸음수를 채우면 앱이 열립니다\n자연스러운 디지털 디톡스를 경험하세요",
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
                        color = Color(0xFF333333),
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
                                    .border(2.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                    .background(Color.White, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = iconList[obstacle.iconIndex]),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(Color(0xFF333333))
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
                                    color = Color(0xFF333333),
                                    topLeft = Offset(size.width * 0.35f, size.height * 0.5f),
                                    size = Size(size.width * 0.3f, size.height * 0.5f)
                                )
                                // Tree top
                                drawRect(
                                    color = Color(0xFF333333),
                                    topLeft = Offset(0f, size.height * 0.1f),
                                    size = Size(size.width, size.height * 0.5f)
                                )
                                drawRect(
                                    color = Color(0xFF333333),
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
                                    .border(2.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
                                    .background(Color.White, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = iconList[obstacle.iconIndex]),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(Color(0xFF333333))
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
                                    color = Color(0xFF333333),
                                    topLeft = Offset(size.width * 0.3f, size.height * 0.2f),
                                    size = Size(size.width * 0.4f, size.height * 0.8f)
                                )
                                // 왼쪽 팔
                                drawRect(
                                    color = Color(0xFF333333),
                                    topLeft = Offset(0f, size.height * 0.4f),
                                    size = Size(size.width * 0.3f, size.height * 0.15f)
                                )
                                drawRect(
                                    color = Color(0xFF333333),
                                    topLeft = Offset(0f, size.height * 0.25f),
                                    size = Size(size.width * 0.15f, size.height * 0.3f)
                                )
                                // 오른쪽 팔
                                drawRect(
                                    color = Color(0xFF333333),
                                    topLeft = Offset(size.width * 0.7f, size.height * 0.5f),
                                    size = Size(size.width * 0.3f, size.height * 0.15f)
                                )
                                drawRect(
                                    color = Color(0xFF333333),
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
                                    color = Color(0xFF333333),
                                    topLeft = Offset(0f, size.height * 0.3f),
                                    size = Size(size.width, size.height * 0.7f)
                                )
                                drawRect(
                                    color = Color(0xFF333333),
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
                            text = "탭해서 점프!",
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
                color = MockupColors.Blue,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                fontSize = 14.sp,
                color = MockupColors.Red,
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
                            text = "Google 로그인",
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
                            text = "로그인 완료",
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
                    text = "[DEBUG] 로그인 없이 테스트",
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
        PetPersonalityV2.LOYAL -> "권한 좀 줘."
        PetPersonalityV2.TSUNDERE -> "뭐, 권한이 필요해."
        PetPersonalityV2.FOODIE -> "권한 부탁! 오네가이~"
        PetPersonalityV2.PLAYFUL -> "권한 좀 줘봐"
        PetPersonalityV2.TIMID -> "저, 권한이 필요해요..."
        PetPersonalityV2.CLUMSY -> "권한 설정 화이팅!"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "권한 설정",
        buttonText = "다음",
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
                title = "걸음 측정",
                description = "걸음 수를 측정합니다",
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
                    title = "알림",
                    description = "진행 상황을 알려드려요",
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
            text = "걸음 측정 권한은 필수입니다",
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
                color = MockupColors.Blue
            )
        } else {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Border),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("허용", fontSize = 14.sp, color = Color.White)
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
        PetPersonalityV2.LOYAL -> "피트니스 앱 연결해."
        PetPersonalityV2.TSUNDERE -> "연결 안 해도 되긴 해..."
        PetPersonalityV2.FOODIE -> "피트니스 연결! 가보자고~"
        PetPersonalityV2.PLAYFUL -> "피트니스 연결해봐"
        PetPersonalityV2.TIMID -> "연결하면 좋을 것 같아요..."
        PetPersonalityV2.CLUMSY -> "연결하면 더 정확해!"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "피트니스 앱 연결",
        buttonText = "나중에 하기",
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
                text = "발견된 피트니스 앱",
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
                            Text("설치됨 ✓", fontSize = 12.sp, color = MockupColors.Blue)
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
                        text = if (isConnecting) "연결 중..." else "연결하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // 앱이 없을 때
            Text(
                text = "피트니스 앱을 찾을 수 없습니다\n기본 센서를 사용합니다",
                fontSize = 14.sp,
                color = MockupColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
        PetPersonalityV2.LOYAL -> "접근성 ON 해."
        PetPersonalityV2.TSUNDERE -> "접근성 켜줘... 부탁이야."
        PetPersonalityV2.FOODIE -> "접근성 켜줘! 오네가이~"
        PetPersonalityV2.PLAYFUL -> "접근성 켜줘"
        PetPersonalityV2.TIMID -> "접근성을 켜주세요..."
        PetPersonalityV2.CLUMSY -> "접근성 설정 화이팅!"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "앱 제어 설정",
        buttonText = "설정 화면으로",
        onButtonClick = {
            hapticManager?.click()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
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
                text = "설정 방법",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Text(
                text = "1. 아래 버튼을 눌러 설정 화면으로\n2. 'rebon' 찾기\n3. rebon을 ON으로 전환\n4. 확인 버튼 누르기",
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ON 확인되면 자동으로 다음 단계로!",
            fontSize = 13.sp,
            color = MockupColors.Blue,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
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
        PetPersonalityV2.LOYAL -> "제어할 앱 골라."
        PetPersonalityV2.TSUNDERE -> "앱 선택해... 빨리."
        PetPersonalityV2.FOODIE -> "앱 선택! 고고~"
        PetPersonalityV2.PLAYFUL -> "앱 골라봐"
        PetPersonalityV2.TIMID -> "앱을 선택해주세요..."
        PetPersonalityV2.CLUMSY -> "어떤 앱을 제어할까?"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "제어할 앱 선택",
        buttonText = if (selectedApps.isEmpty()) "1개 이상 선택" else "다음 (${selectedApps.size}개)",
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
                text = "✓ ${selectedApps.size}개 선택됨",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.Blue,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
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
                                text = category.displayName,
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
            PetPersonalityV2.LOYAL -> "좋아. 해봤군."
            PetPersonalityV2.TSUNDERE -> "뭐, 괜찮네."
            PetPersonalityV2.FOODIE -> "잘함! 나이스~"
            PetPersonalityV2.PLAYFUL -> "잘했다 아이가~"
            PetPersonalityV2.TIMID -> "잘 하셨어요...!"
            PetPersonalityV2.CLUMSY -> "완벽해!"
        }
        testStarted -> "확인 중..."
        else -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> "앱 실행해봐."
            PetPersonalityV2.TSUNDERE -> "앱 실행해봐... 뭐해?"
            PetPersonalityV2.FOODIE -> "앱 실행해봐! 고고~"
            PetPersonalityV2.PLAYFUL -> "앱 실행해봐"
            PetPersonalityV2.TIMID -> "앱을 실행해보세요..."
            PetPersonalityV2.CLUMSY -> "앱 실행 테스트!"
        }
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = if (canProceed) "체험 완료!" else "앱 차단 체험",
        buttonText = if (canProceed) "다음" else "앱을 실행해보세요",
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
                    text = "✓ 차단 체험 완료!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.Blue
                )
                Text(
                    text = "이제 걸어서 해제해볼까요?",
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary
                )
            } else {
                Text(
                    text = "테스트 방법",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = "1. 홈 버튼을 눌러 나가기\n2. 선택한 앱 실행\n3. 차단 메시지 확인\n4. rebon으로 돌아오기",
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
        PetPersonalityV2.LOYAL -> "목표를 정해."
        PetPersonalityV2.TSUNDERE -> "목표... 적당히 해."
        PetPersonalityV2.FOODIE -> "목표 정하자! ㄱㄱ!"
        PetPersonalityV2.PLAYFUL -> "목표 정해봐"
        PetPersonalityV2.TIMID -> "목표를 정해주세요..."
        PetPersonalityV2.CLUMSY -> "목표 설정 화이팅!"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "목표 설정",
        buttonText = "다음",
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
                text = "${stepsSliderValue.toInt()}보",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "50보 ~ 70보 (체험용)",
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
                    text = "목표 달성하면",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
                Text(
                    text = "차단된 앱이 해제됩니다!",
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
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // 월~금
    val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> "제어할 요일 골라."
        PetPersonalityV2.TSUNDERE -> "요일... 빨리 골라."
        PetPersonalityV2.FOODIE -> "요일 선택! 고고~"
        PetPersonalityV2.PLAYFUL -> "요일 골라봐"
        PetPersonalityV2.TIMID -> "요일을 선택해주세요..."
        PetPersonalityV2.CLUMSY -> "어떤 요일에 제어할까?"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "제어 요일 선택",
        buttonText = "다음",
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
                    text = "추천: 평일(월~금)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            }
            Text(
                text = "주말은 자유롭게!",
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
    val periods = listOf(
        "morning" to "아침\n06-12시",
        "afternoon" to "점심\n12-18시",
        "evening" to "저녁\n18-22시",
        "night" to "밤\n22-06시"
    )

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> "차단 시간 정해."
        PetPersonalityV2.TSUNDERE -> "시간... 골라."
        PetPersonalityV2.FOODIE -> "시간 정하자! 렛츠고~"
        PetPersonalityV2.PLAYFUL -> "시간 정해봐"
        PetPersonalityV2.TIMID -> "시간을 정해주세요..."
        PetPersonalityV2.CLUMSY -> "언제 제어할까?"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "차단 시간대",
        buttonText = "다음",
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
                text = "선택하지 않으면 차단되지 않습니다",
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

    // 튜토리얼 진입 시 Health Connect 강제 비활성화 & 종료 시 복원
    DisposableEffect(Unit) {
        // 이전 Health Connect 설정 백업
        val originalUseHealthConnect = preferenceManager.useHealthConnect()
        android.util.Log.d("WalkingTest", "💾 Backup original useHealthConnect: $originalUseHealthConnect")

        // 강제로 기본 센서 사용하도록 설정
        preferenceManager.setUseHealthConnect(false)
        android.util.Log.d("WalkingTest", "🔧 Forced useHealthConnect = false for tutorial")

        // StepCounterService 재시작 (설정 변경 반영)
        StepCounterService.stop(context)
        Thread.sleep(100)
        StepCounterService.start(context)
        android.util.Log.d("WalkingTest", "🔄 Restarted StepCounterService with basic sensor")

        onDispose {
            // 튜토리얼 종료 시 원래 설정 복원
            preferenceManager.setUseHealthConnect(originalUseHealthConnect)
            android.util.Log.d("WalkingTest", "🔙 Restored useHealthConnect to: $originalUseHealthConnect")

            // StepCounterService 재시작 (설정 복원 반영)
            StepCounterService.stop(context)
            Thread.sleep(100)
            StepCounterService.start(context)
            android.util.Log.d("WalkingTest", "🔄 Restarted StepCounterService after tutorial")
        }
    }

    // Baseline 초기화
    LaunchedEffect(Unit) {
        // 서비스 시작 대기 후 baseline 업데이트
        kotlinx.coroutines.delay(500)
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
                // 목표 달성 알림 발송
                notificationHelper.showTutorialGoalAchievedNotification(targetSteps)
                android.util.Log.d("WalkingTest", "🎉 Goal achieved! Notification sent.")
            }
            delay(1000) // 튜토리얼에서는 즉각적 피드백을 위해 1초
        }
    }

    val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)

    val speechText = when {
        goalAchieved -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> "잘했어."
            PetPersonalityV2.TSUNDERE -> "뭐, 괜찮네."
            PetPersonalityV2.FOODIE -> "대박! 대단해ㅋㅋ"
            PetPersonalityV2.PLAYFUL -> "잘했다 아이가~"
            PetPersonalityV2.TIMID -> "정말 잘하셨어요...!"
            PetPersonalityV2.CLUMSY -> "완벽해! 최고야!"
        }
        currentSteps == 0 -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> "가볍게 산책하고 오면 채워질 거야."
            PetPersonalityV2.TSUNDERE -> "산책이나 하고 와. 그럼 차."
            PetPersonalityV2.FOODIE -> "가볍게 산책하고 오면 돼~"
            PetPersonalityV2.PLAYFUL -> "산책하고 오면 완성이야!"
            PetPersonalityV2.TIMID -> "산책하고 오시면... 채워질 거예요..."
            PetPersonalityV2.CLUMSY -> "산책하고 오면 끝! 화이팅!"
        }
        else -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> "걸어."
            PetPersonalityV2.TSUNDERE -> "걸어... 빨리."
            PetPersonalityV2.FOODIE -> "걸어보자! ㄱㄱ~"
            PetPersonalityV2.PLAYFUL -> "걸어봐"
            PetPersonalityV2.TIMID -> "걸어주세요..."
            PetPersonalityV2.CLUMSY -> "걷기 화이팅!"
        }
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = if (goalAchieved) "목표 달성!" else "${targetSteps}보 걸어보세요!",
        buttonText = if (goalAchieved) "다음" else "걸음 수 달성 필요",
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
                text = "/ $targetSteps 보",
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
                        text = "Health App과 동기화에 몇 초 소요될 수 있어요",
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "걸음 수가 안 올라가면 삼성헬스/Health Connect 문제예요",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "걸음수 동기화에 약 30초 정도 걸려요",
                        fontSize = 12.sp,
                        color = MockupColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "완료되면 알림으로 알려드릴게요",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "가볍게 산책하고 돌아오세요!",
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
                        hapticManager?.lightClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("지금은 걷기 어려워요.", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

// =====================================================
// STEP 13: Unlocked (잠금 해제)
// =====================================================
@Composable
private fun UnlockedStep(
    petType: PetTypeV2,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> "해제됐어."
        PetPersonalityV2.TSUNDERE -> "뭐, 해제됐네."
        PetPersonalityV2.FOODIE -> "해제됐어! 야타~"
        PetPersonalityV2.PLAYFUL -> "해제됐다 아이가~"
        PetPersonalityV2.TIMID -> "해제되었어요...!"
        PetPersonalityV2.CLUMSY -> "앱이 해제됐어!"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "앱이 해제되었어요!",
        buttonText = "다음",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelIcon(iconName = "icon_star", size = 24.dp)
                Text(
                    text = "rebon의 핵심",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary
                )
            }
            Text(
                text = "매일 목표를 달성하면 앱을 자유롭게!\n실패하면 차단됩니다.",
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                lineHeight = 22.sp
            )
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
        PetPersonalityV2.LOYAL -> "급할 땐 쉬어가."
        PetPersonalityV2.TSUNDERE -> "급하면... 쉬어가."
        PetPersonalityV2.FOODIE -> "급하면 쉬어가! 다이죠부~"
        PetPersonalityV2.PLAYFUL -> "급하면 쉬어가"
        PetPersonalityV2.TIMID -> "급하시면 쉬어가세요..."
        PetPersonalityV2.CLUMSY -> "가끔은 쉬어가도 돼!"
    }

    TutorialStepLayout(
        petType = petType,
        speechText = speechText,
        instructionText = "잠시 쉬어가기",
        buttonText = "다음",
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
                text = "15분 휴식 모드",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Text(
                text = "• 급한 일이 있을 때 15분간 앱 사용 가능\n• 하루에 1회만 사용 가능\n• 15분 후 자동으로 다시 차단",
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

// =====================================================
// STEP 16: Widget Setup (위젯 설정) - 마지막 단계
// =====================================================
@Composable
private fun WidgetSetupStep(
    petType: PetTypeV2,
    petName: String,
    hapticManager: HapticManager?,
    onComplete: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    val displayPetSize = 140.dp
    val stripeWidth = 4.dp

    val speechText = when (petType.personality) {
        PetPersonalityV2.LOYAL -> "위젯 추가해."
        PetPersonalityV2.TSUNDERE -> "위젯... 추가해줘."
        PetPersonalityV2.FOODIE -> "위젯 추가! 고고~"
        PetPersonalityV2.PLAYFUL -> "위젯 추가해봐"
        PetPersonalityV2.TIMID -> "위젯을 추가해주세요..."
        PetPersonalityV2.CLUMSY -> "위젯으로 한눈에 확인!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 72.dp),  // 3버튼 네비게이션 고려
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text(
            text = "rebon",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display area
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

        Text(
            text = "위젯 설정",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 위젯 안내
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "위젯 추가 방법",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Text(
                text = "1. 홈 화면 길게 누르기\n2. 위젯 선택\n3. rebon 위젯 찾기\n4. 홈 화면에 추가",
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelIcon(iconName = "icon_light_bulb", size = 14.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "위젯으로 걸음 수를 빠르게 확인하세요!",
                fontSize = 13.sp,
                color = MockupColors.TextMuted
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 완료 버튼
        MockupButton(
            text = "시작하기!",
            onClick = onComplete
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

    // 입장 애니메이션 상태
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
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
            PetPersonalityV2.LOYAL -> "공짜로 가는 거야. 준비해."
            PetPersonalityV2.TSUNDERE -> "뭐, 운 좋네. 공짜래."
            PetPersonalityV2.FOODIE -> "우와 공짜야! 야타~!"
            PetPersonalityV2.PLAYFUL -> "공짜라카네! 좋다 아이가!"
            PetPersonalityV2.TIMID -> "무, 무료래요...! 다행이에요..."
            PetPersonalityV2.CLUMSY -> "공짜라니! 최고의 시작이야!"
        }
        else -> when (petType.personality) {
            PetPersonalityV2.LOYAL -> "하루 100원으로\n꿈을 이뤄봐."
            PetPersonalityV2.TSUNDERE -> "하루 100원이면 돼...\n꿈 이뤄볼래?"
            PetPersonalityV2.FOODIE -> "하루 100원으로\n꿈을 이뤄보자~!"
            PetPersonalityV2.PLAYFUL -> "하루 100원이면\n꿈 이룰 수 있다이~"
            PetPersonalityV2.TIMID -> "하, 하루 100원으로...\n꿈을 이뤄봐요...!"
            PetPersonalityV2.CLUMSY -> "하루 100원으로 꿈 이루기!\n완전 좋아!"
        }
    }

    val buttonText = when {
        isProcessing -> "결제 중..."
        isPromoFree -> "무료로 시작하기"
        else -> "7일 무료로 시작하기"
    }

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
                    errorMessage = "Activity를 찾을 수 없습니다"
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
                                    errorMessage = "구독 정보 저장 실패"
                                    isProcessing = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "오류: ${e.message}"
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
                errorMessage = "오류: ${e.message}"
                isProcessing = false
            }
        }
    }

    val kenneyFont = rememberKenneyFont()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 로고 (작게) - DEBUG: 길게 누르면 건너뛰기
        Text(
            text = "rebon",
            fontSize = 20.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextMuted,
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

        Spacer(modifier = Modifier.height(4.dp))

        // 헤드라인 - 변화 강조
        Text(
            text = "한 달 뒤, 달라진 나",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                            baseSizeDp = 88,
                            monochrome = true
                        )
                    } else {
                        PetSpriteV2WithGlow(
                            petType = petType,
                            stage = PetGrowthStage.BABY,
                            animationType = PetAnimationTypeV2.RUN,
                            size = 88.dp,
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
                // 펫 이름 (펫에 더 가깝게)
                Text(
                    text = petName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.TextPrimary,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isPromoFree) {
            // 프로모션 무료 상태
            Text(
                text = "무료로 시작!",
                fontSize = 22.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPromoGuest) "1달간 모든 기능 무료!" else "친구 1명도 무료 초대 가능!",
                fontSize = 16.sp,
                color = MockupColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        } else {
            // 7일 무료 체험 포함
            Text(
                text = "7일 무료 체험 포함",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MockupColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                    .height(90.dp),
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 12.dp
            ) { page ->
                val isYearly = page == 0
                val isSelected = pagerState.currentPage == page
                // 1일 가격 계산: 연간 39000/365 ≈ 107원, 월간 3900/30 = 130원
                val dailyPrice = if (isYearly) "107" else "130"

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
                                text = if (isYearly) "39,000" else "3,900",
                                fontSize = 24.sp,
                                fontFamily = kenneyFont,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                            Text(
                                text = if (isYearly) "원/년" else "원/월",
                                fontSize = 11.sp,
                                color = MockupColors.TextMuted,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Text(
                            text = "하루 ${dailyPrice}원",
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

        Spacer(modifier = Modifier.height(16.dp))

        // 혜택 구분선
        Text(
            text = "- 혜택 -",
            fontSize = 13.sp,
            color = MockupColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 혜택 리스트 (순차 fade in 애니메이션)
        val benefits = listOf(
            Triple("icon_heart", "AI 펫 케어", "매일 대화하며 함께 성장해요"),
            Triple("icon_lock", "스마트 앱 차단", "목표 달성 전까지 유혹 차단"),
            Triple("icon_target", "홈 위젯", "홈 화면에서 바로 확인"),
            Triple("icon_trophy",
                if (selectedPlan == BillingManager.SubscriptionType.YEARLY) "친구 12명 초대" else "친구 1명 초대",
                "친구도 무료로 시작 가능")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "1,000+ 사용자와 함께하고 있어요",
            fontSize = 11.sp,
            color = MockupColors.TextMuted
        )

        // 오류 메시지
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                fontSize = 12.sp,
                color = MockupColors.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    text = if (isPromoApplied) "적용 완료" else "초대 코드",
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
                        placeholder = { Text("코드 입력", fontSize = 14.sp) },
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
                                promoMessage = "확인 중..."
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
                        Text("적용", fontWeight = FontWeight.Bold)
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
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PixelIcon(iconName = icon, size = 28.dp)
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

