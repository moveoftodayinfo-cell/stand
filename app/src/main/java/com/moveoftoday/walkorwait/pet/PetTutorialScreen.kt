package com.moveoftoday.walkorwait.pet

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Complete Pet Onboarding Flow - 16 Steps:
 *
 * NO DOTS (0-2):
 * 0. Pet Selection
 * 1. Pet Name Input
 * 2. Tutorial All-in-One (함께 할 것 설명)
 *
 * WITH DOTS (3-14, 12 dots total):
 * 3. Permission Settings (권한 설정) - dot 0
 * 4. Fitness App Connection (피트니스 연결) - dot 1
 * 5. Accessibility (접근성 권한) - dot 2
 * 6. App Selection (앱 선택) - dot 3
 * 7. Test Blocking (차단 테스트) - dot 4
 * 8. Goal Input (목표 입력) - dot 5
 * 9. Walking Test (걷기 테스트) - dot 6
 * 10. Unlocked (잠금 해제) - dot 7
 * 11. Control Days (제어 요일) - dot 8
 * 12. Block Time (차단 시간대) - dot 9
 * 13. Emergency Button (긴급 버튼) - dot 10
 * 14. Payment (결제) - dot 11
 *
 * NO DOTS (15):
 * 15. Widget Setup (위젯 설정)
 */
@Composable
fun PetOnboardingScreen(
    onComplete: (PetType, String) -> Unit,
    hapticManager: HapticManager? = null,
    preferenceManager: PreferenceManager? = null
) {
    val context = LocalContext.current
    val prefManager = preferenceManager ?: remember { PreferenceManager(context) }

    // 저장된 펫 정보 불러오기
    val savedPetTypeName = remember { prefManager.getPetType() }
    val savedPetName = remember { prefManager.getPetName() }
    val savedPetType = remember {
        if (savedPetTypeName != null) PetType.entries.find { it.name == savedPetTypeName } else null
    }

    // 저장된 단계 불러오기 (펫 정보가 있어야만 복원)
    val savedStep = remember {
        val step = prefManager.getTutorialCurrentStep()
        // 펫 정보가 필요한 단계(3 이상)인데 펫 정보가 없으면 0으로 리셋
        if (step >= 3 && savedPetType == null) 0 else step
    }

    var currentStep by remember { mutableIntStateOf(savedStep) }
    var selectedPetType by remember { mutableStateOf(savedPetType) }
    var petName by remember { mutableStateOf(if (savedStep > 1 && savedPetName.isNotBlank()) savedPetName else "") }

    // 단계 변경 시 저장
    LaunchedEffect(currentStep) {
        prefManager.saveTutorialCurrentStep(currentStep)
    }

    // 네비게이션 닷 계산 (Step 3-14는 닷 표시, 12개)
    val showDots = currentStep in 3..14
    val dotStep = if (showDots) currentStep - 3 else 0
    val totalDots = 12

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        when (currentStep) {
            // === NO DOTS (0-2) ===
            0 -> PetSelectionStep(
                selectedPet = selectedPetType,
                onPetSelected = {
                    selectedPetType = it
                    // 펫 선택 시 바로 저장
                    prefManager.savePetType(it.name)
                    // 위젯 업데이트
                    StepWidgetProvider.updateAllWidgets(context)
                },
                onNext = {
                    hapticManager?.click()
                    currentStep = 1
                },
                hapticManager = hapticManager
            )
            1 -> PetNameInputStep(
                petType = selectedPetType!!,
                currentName = petName,
                onNameChanged = {
                    petName = it
                    // 이름 입력 시 바로 저장
                    prefManager.savePetName(it)
                },
                onNext = {
                    hapticManager?.click()
                    currentStep = 2
                },
                hapticManager = hapticManager
            )
            2 -> TutorialAllInOneStep(
                petType = selectedPetType!!,
                petName = petName,
                onComplete = {
                    hapticManager?.click()
                    currentStep = 3
                }
            )

            // === WITH DOTS (3-14) ===
            3 -> PermissionSettingsStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 4
                }
            )
            4 -> FitnessConnectionStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 5
                }
            )
            5 -> AccessibilityStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 6
                }
            )
            6 -> AppSelectionStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 7
                }
            )
            7 -> TestBlockingStep(
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
                    currentStep = 8
                }
            )
            8 -> GoalInputStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 9
                }
            )
            9 -> WalkingTestStep(
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
            10 -> UnlockedStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 11
                }
            )
            11 -> ControlDaysStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 12
                }
            )
            12 -> BlockTimeStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                preferenceManager = prefManager,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 13
                }
            )
            13 -> EmergencyButtonStep(
                petType = selectedPetType!!,
                petName = petName,
                dotStep = dotStep,
                totalDots = totalDots,
                hapticManager = hapticManager,
                onNext = {
                    hapticManager?.click()
                    currentStep = 14
                }
            )
            14 -> PaymentStep(
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

            // === NO DOTS (15) ===
            15 -> WidgetSetupStep(
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
                    prefManager.setPaidDeposit(true)
                    // 튜토리얼 진행 단계 초기화
                    prefManager.clearTutorialCurrentStep()
                    // 실제 목표 설정 필요 플래그
                    prefManager.setNeedsRealGoalSetup(true)
                    onComplete(selectedPetType!!, petName)
                }
            )
        }
    }
}

/**
 * Step 1: Pet Selection - basic.png 목업 + Game Boy LCD 스타일
 */
@Composable
private fun PetSelectionStep(
    selectedPet: PetType?,
    onPetSelected: (PetType) -> Unit,
    onNext: () -> Unit,
    hapticManager: HapticManager?
) {
    val kenneyFont = rememberKenneyFont()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - Kenney Font
        Text(
            text = "Stand",
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
                    // Speech bubble
                    val greeting = PetDialogues.getWelcomeMessage(selectedPet.personality, "")
                    SpeechBubble(
                        text = greeting,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Pet sprite with synced glow
                    PetSpriteWithSyncedGlow(
                        petType = selectedPet,
                        isWalking = false,
                        size = displayPetSize,
                        monochrome = true,
                        frameDurationMs = 500,
                        enableRandomAnimation = true
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
            // Row 1
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PetType.entries.take(3).forEach { petType ->
                    SmallPetCard(
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
            // Row 2
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PetType.entries.drop(3).take(3).forEach { petType ->
                    SmallPetCard(
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
                text = getPetDescription(selectedPet),
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
 * 펫 특징 설명 (3줄)
 */
private fun getPetDescription(petType: PetType): String {
    return when (petType) {
        PetType.DOG1 -> "듬직하고 멋있는 상남자 스타일\n말수는 적지만 행동으로 보여주는 타입\n묵묵히 당신 곁을 지켜줄 거예요"
        PetType.DOG2 -> "갓생러 지망 강아지\n간바레! 이쿠요! 응원이 특기\n같이 있으면 텐션 업 보장"
        PetType.CAT1 -> "겉은 차갑지만 속은 따뜻한 츤데레\n관심 없는 척하지만 사실 다 챙겨요\n은근히 당신 걱정을 많이 해요"
        PetType.CAT2 -> "정 많은 경상도 사투리 고양이\n구수한 말투로 친근하게 다가와요\n푸근한 매력에 빠지게 될 거예요"
        PetType.RAT -> "소심하지만 마음은 따뜻해요\n조심스럽게 당신에게 다가가요\n천천히 친해지면 든든한 친구가 돼요"
        PetType.BIRD -> "언제나 밝고 긍정적인 에너지\n힘들 때 용기를 북돋아 줘요\n함께라면 매일이 즐거워요"
    }
}

/**
 * Small pet card for selection - 원래 크기, 펫만 크게
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
 * Step 2: Pet Name Input - basic.png 목업 정확히 따름
 */
@Composable
private fun PetNameInputStep(
    petType: PetType,
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
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - 고정
        Text(
            text = "Stand",
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
                // Pet sprite with synced glow
                PetSpriteWithSyncedGlow(
                    petType = petType,
                    isWalking = false,
                    size = displayPetSize,
                    monochrome = true,
                    frameDurationMs = 500,
                    enableRandomAnimation = true
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
 * Tutorial All-in-One: 3가지 튜토리얼 항목을 한 화면에
 */
@Composable
private fun TutorialAllInOneStep(
    petType: PetType,
    petName: String,
    onComplete: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    val displayPetSize = 140.dp
    val stripeWidth = 4.dp

    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "준비됐어. 시작하자."
        PetPersonality.CUTE -> "같이 간바루! 이쿠요~"
        PetPersonality.TSUNDERE -> "뭐, 잘 부탁해."
        PetPersonality.DIALECT -> "자, 시작해보이소!"
        PetPersonality.TIMID -> "잘, 잘 부탁드려요..."
        PetPersonality.POSITIVE -> "우리 함께 화이팅!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - 고정
        Text(
            text = "Stand",
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
                // Pet sprite with synced glow
                PetSpriteWithSyncedGlow(
                    petType = petType,
                    isWalking = false,
                    size = displayPetSize,
                    monochrome = true,
                    frameDurationMs = 500,
                    enableRandomAnimation = true
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

        // Action button - 고정
        MockupButton(
            text = "시작하기!",
            onClick = onComplete
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
// STEP 3: Permission Settings (권한 설정)
// =====================================================
@Composable
private fun PermissionSettingsStep(
    petType: PetType,
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
        if (isGranted) hapticManager?.success()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "권한 좀 줘."
        PetPersonality.CUTE -> "권한 부탁! 오네가이~"
        PetPersonality.TSUNDERE -> "뭐, 권한이 필요해."
        PetPersonality.DIALECT -> "권한 좀 줘보이소~"
        PetPersonality.TIMID -> "저, 권한이 필요해요..."
        PetPersonality.POSITIVE -> "권한 설정 화이팅!"
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
// STEP 4: Fitness App Connection (피트니스 앱 연결)
// =====================================================
@Composable
private fun FitnessConnectionStep(
    petType: PetType,
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
        PetPersonality.TOUGH -> "피트니스 앱 연결해."
        PetPersonality.CUTE -> "피트니스 연결! 이쿠요~"
        PetPersonality.TSUNDERE -> "연결 안 해도 되긴 해..."
        PetPersonality.DIALECT -> "피트니스 연결해보이소~"
        PetPersonality.TIMID -> "연결하면 좋을 것 같아요..."
        PetPersonality.POSITIVE -> "연결하면 더 정확해!"
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
// STEP 5: Accessibility (접근성 권한)
// =====================================================
@Composable
private fun AccessibilityStep(
    petType: PetType,
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
        PetPersonality.TOUGH -> "접근성 ON 해."
        PetPersonality.CUTE -> "접근성 켜줘! 오네가이~"
        PetPersonality.TSUNDERE -> "접근성 켜줘... 부탁이야."
        PetPersonality.DIALECT -> "접근성 켜주이소~"
        PetPersonality.TIMID -> "접근성을 켜주세요..."
        PetPersonality.POSITIVE -> "접근성 설정 화이팅!"
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
                text = "1. 아래 버튼을 눌러 설정 화면으로\n2. 'Stand' 찾기\n3. Stand를 ON으로 전환\n4. 확인 버튼 누르기",
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
// STEP 6: App Selection (앱 선택)
// =====================================================
@Composable
private fun AppSelectionStep(
    petType: PetType,
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
        PetPersonality.TOUGH -> "제어할 앱 골라."
        PetPersonality.CUTE -> "앱 선택! 고고~"
        PetPersonality.TSUNDERE -> "앱 선택해... 빨리."
        PetPersonality.DIALECT -> "앱 골라보이소~"
        PetPersonality.TIMID -> "앱을 선택해주세요..."
        PetPersonality.POSITIVE -> "어떤 앱을 제어할까?"
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
// STEP 7: Test Blocking (차단 테스트)
// =====================================================
@Composable
private fun TestBlockingStep(
    petType: PetType,
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
            PetPersonality.TOUGH -> "좋아. 해봤군."
            PetPersonality.CUTE -> "잘함! 나이스~"
            PetPersonality.TSUNDERE -> "뭐, 괜찮네."
            PetPersonality.DIALECT -> "잘했다 아이가~"
            PetPersonality.TIMID -> "잘 하셨어요...!"
            PetPersonality.POSITIVE -> "완벽해!"
        }
        testStarted -> "확인 중..."
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "앱 실행해봐."
            PetPersonality.CUTE -> "앱 실행해봐! 고고~"
            PetPersonality.TSUNDERE -> "앱 실행해봐... 뭐해?"
            PetPersonality.DIALECT -> "앱 실행해보이소~"
            PetPersonality.TIMID -> "앱을 실행해보세요..."
            PetPersonality.POSITIVE -> "앱 실행 테스트!"
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
                    text = "1. 홈 버튼을 눌러 나가기\n2. 선택한 앱 실행\n3. 차단 메시지 확인\n4. Stand로 돌아오기",
                    fontSize = 14.sp,
                    color = MockupColors.TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// =====================================================
// STEP 8: Goal Input (목표 설정)
// =====================================================
@Composable
private fun GoalInputStep(
    petType: PetType,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    var stepsSliderValue by remember { mutableFloatStateOf(60f) }

    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "목표를 정해."
        PetPersonality.CUTE -> "목표 정하자! 간바!"
        PetPersonality.TSUNDERE -> "목표... 적당히 해."
        PetPersonality.DIALECT -> "목표 정해보이소~"
        PetPersonality.TIMID -> "목표를 정해주세요..."
        PetPersonality.POSITIVE -> "목표 설정 화이팅!"
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
// STEP 9: Control Days (제어 요일)
// =====================================================
@Composable
private fun ControlDaysStep(
    petType: PetType,
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
        PetPersonality.TOUGH -> "제어할 요일 골라."
        PetPersonality.CUTE -> "요일 선택! 고고~"
        PetPersonality.TSUNDERE -> "요일... 빨리 골라."
        PetPersonality.DIALECT -> "요일 골라보이소~"
        PetPersonality.TIMID -> "요일을 선택해주세요..."
        PetPersonality.POSITIVE -> "어떤 요일에 제어할까?"
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
// STEP 10: Block Time (차단 시간대)
// =====================================================
@Composable
private fun BlockTimeStep(
    petType: PetType,
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
        PetPersonality.TOUGH -> "차단 시간 정해."
        PetPersonality.CUTE -> "시간 정하자! 렛츠고~"
        PetPersonality.TSUNDERE -> "시간... 골라."
        PetPersonality.DIALECT -> "시간 정해보이소~"
        PetPersonality.TIMID -> "시간을 정해주세요..."
        PetPersonality.POSITIVE -> "언제 제어할까?"
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
// STEP 11: Walking Test (걷기 테스트)
// =====================================================
@Composable
private fun WalkingTestStep(
    petType: PetType,
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

    val baselineSteps = remember { repository.getTodaySteps() }
    var currentSteps by remember { mutableIntStateOf(0) }
    val targetSteps = repository.getGoal()
    var goalAchieved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (!goalAchieved) {
            val rawSteps = repository.getTodaySteps()
            val newSteps = maxOf(0, rawSteps - baselineSteps)
            currentSteps = newSteps
            if (currentSteps >= targetSteps && !goalAchieved) {
                goalAchieved = true
                hapticManager?.goalAchieved()
            }
            delay(1000)
        }
    }

    val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)

    val speechText = when {
        goalAchieved -> when (petType.personality) {
            PetPersonality.TOUGH -> "잘했어."
            PetPersonality.CUTE -> "스고이! 대단해!"
            PetPersonality.TSUNDERE -> "뭐, 괜찮네."
            PetPersonality.DIALECT -> "잘했다 아이가~"
            PetPersonality.TIMID -> "정말 잘하셨어요...!"
            PetPersonality.POSITIVE -> "완벽해! 최고야!"
        }
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "걸어."
            PetPersonality.CUTE -> "걸어보자! 이쿠요~"
            PetPersonality.TSUNDERE -> "걸어... 빨리."
            PetPersonality.DIALECT -> "걸어보이소~"
            PetPersonality.TIMID -> "걸어주세요..."
            PetPersonality.POSITIVE -> "걷기 화이팅!"
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

            Spacer(modifier = Modifier.height(16.dp))

            // 테스트 버튼
            if (!goalAchieved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            repository.saveTodaySteps(baselineSteps + currentSteps + 5)
                            hapticManager?.lightClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+5", color = Color.White)
                    }
                    Button(
                        onClick = {
                            repository.saveTodaySteps(baselineSteps + targetSteps)
                            hapticManager?.success()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MockupColors.Border),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("달성", color = Color.White)
                    }
                }
            }
        }
    }
}

// =====================================================
// STEP 12: Unlocked (잠금 해제)
// =====================================================
@Composable
private fun UnlockedStep(
    petType: PetType,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "해제됐어."
        PetPersonality.CUTE -> "해제됐어! 야타~"
        PetPersonality.TSUNDERE -> "뭐, 해제됐네."
        PetPersonality.DIALECT -> "해제됐다 아이가~"
        PetPersonality.TIMID -> "해제되었어요...!"
        PetPersonality.POSITIVE -> "앱이 해제됐어!"
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
                    text = "Stand의 핵심",
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
// STEP 13: Emergency Button (긴급 버튼)
// =====================================================
@Composable
private fun EmergencyButtonStep(
    petType: PetType,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    hapticManager: HapticManager?,
    onNext: () -> Unit
) {
    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "급할 땐 쉬어가."
        PetPersonality.CUTE -> "급하면 쉬어가! 다이죠부~"
        PetPersonality.TSUNDERE -> "급하면... 쉬어가."
        PetPersonality.DIALECT -> "급하면 쉬어가이소~"
        PetPersonality.TIMID -> "급하시면 쉬어가세요..."
        PetPersonality.POSITIVE -> "가끔은 쉬어가도 돼!"
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
// STEP 15: Widget Setup (위젯 설정) - 마지막 단계
// =====================================================
@Composable
private fun WidgetSetupStep(
    petType: PetType,
    petName: String,
    hapticManager: HapticManager?,
    onComplete: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()
    val displayPetSize = 140.dp
    val stripeWidth = 4.dp

    val speechText = when (petType.personality) {
        PetPersonality.TOUGH -> "위젯 추가해."
        PetPersonality.CUTE -> "위젯 추가! 고고~"
        PetPersonality.TSUNDERE -> "위젯... 추가해줘."
        PetPersonality.DIALECT -> "위젯 추가해보이소~"
        PetPersonality.TIMID -> "위젯을 추가해주세요..."
        PetPersonality.POSITIVE -> "위젯으로 한눈에 확인!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text(
            text = "Stand",
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
                PetSpriteWithSyncedGlow(
                    petType = petType,
                    isWalking = false,
                    size = displayPetSize,
                    monochrome = true,
                    frameDurationMs = 500,
                    enableRandomAnimation = true
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
                text = "1. 홈 화면 길게 누르기\n2. 위젯 선택\n3. Stand 위젯 찾기\n4. 홈 화면에 추가",
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
// STEP 14: Payment (결제) - 네비게이션 닷 표시
// =====================================================
@Composable
private fun PaymentStep(
    petType: PetType,
    petName: String,
    dotStep: Int,
    totalDots: Int,
    preferenceManager: PreferenceManager,
    hapticManager: HapticManager?,
    onNext: () -> Unit
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
    val promoCodeManager = remember { PromoCodeManager(context) }

    val selectedDays = remember { preferenceManager.getControlDays() }
    val selectedPeriods = remember { preferenceManager.getBlockingPeriods() }

    DisposableEffect(Unit) {
        onDispose { billingManager?.destroy() }
    }

    val speechText = when {
        isPromoFree -> when (petType.personality) {
            PetPersonality.TOUGH -> "공짜로 가는 거야. 준비해."
            PetPersonality.CUTE -> "우와 공짜야! 야타~!"
            PetPersonality.TSUNDERE -> "뭐, 운 좋네. 공짜래."
            PetPersonality.DIALECT -> "공짜라카네! 좋다 아이가!"
            PetPersonality.TIMID -> "무, 무료래요...! 다행이에요..."
            PetPersonality.POSITIVE -> "공짜라니! 최고의 시작이야!"
        }
        else -> when (petType.personality) {
            PetPersonality.TOUGH -> "열심히 하면 평생 공짜야.\n나만 믿어."
            PetPersonality.CUTE -> "같이 간바루 하면\n평생 무료! 사이코~"
            PetPersonality.TSUNDERE -> "열심히 하면 돈 안 내도 돼...\n내가 도와줄게."
            PetPersonality.DIALECT -> "열심히 하믄\n평생 공짜라카이!"
            PetPersonality.TIMID -> "저, 저랑 열심히 하면...\n평생 무료예요...!"
            PetPersonality.POSITIVE -> "목표 달성하면 평생 무료!\n같이 해보자!"
        }
    }

    val buttonText = when {
        isProcessing -> "결제 중..."
        isPromoFree -> "무료로 시작하기"
        else -> "결제하고 시작하기"
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
                    preferenceManager.setPaidDeposit(true)
                    preferenceManager.saveTodaySteps(0)

                    val pastDate = java.util.Calendar.getInstance()
                    pastDate.add(java.util.Calendar.DAY_OF_MONTH, -10)
                    preferenceManager.saveTrialStartDate(sdf.format(pastDate.time))
                    pastDate.add(java.util.Calendar.DAY_OF_MONTH, 3)
                    preferenceManager.saveTrialEndDate(sdf.format(pastDate.time))

                    isProcessing = false
                    hapticManager?.success()
                    onNext()
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
                                    purchase = purchase
                                )
                                if (result.isSuccess) {
                                    preferenceManager.saveDeposit(SubscriptionModel.MONTHLY_PRICE)
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
                                    onNext()
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
                billingManager?.startSubscription(activity)

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
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Title - Kenney Font (PetSelectionStep과 동일)
        Text(
            text = "Stand",
            fontSize = 32.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display Area (240dp, 수평 줄무늬 - PetSelectionStep과 동일)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .drawBehind {
                    val stripeHeightPx = 4.dp.toPx()
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
                // Speech bubble (SpeechBubble 컴포넌트 사용)
                SpeechBubble(text = speechText, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Pet sprite with glow (PetSelectionStep과 동일)
                PetSpriteWithSyncedGlow(
                    petType = petType,
                    isWalking = false,
                    size = 140.dp,
                    monochrome = true,
                    frameDurationMs = 500,
                    enableRandomAnimation = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instruction Area - 가격 정보 (22sp, Kenney)
        Text(
            text = if (isPromoFree) "무료로 시작!" else "한 달 동행 ${SubscriptionModel.formatPrice(SubscriptionModel.MONTHLY_PRICE)}",
            fontSize = 22.sp,
            fontFamily = kenneyFont,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 추가 안내
        Text(
            text = if (isPromoFree) "친구 1명도 무료 초대 가능!" else "달성 시 매달 초대 쿠폰 증정",
            fontSize = 16.sp,
            color = MockupColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 혜택 배너
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(iconName = "icon_heart", size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "매일 응원해주는 내 펫",
                    fontSize = 14.sp,
                    color = MockupColors.TextPrimary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(iconName = "icon_lock", size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "딴짓 방지 앱 차단",
                    fontSize = 14.sp,
                    color = MockupColors.TextPrimary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(iconName = "icon_star", size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "95% 달성 시 친구 초대 쿠폰",
                    fontSize = 14.sp,
                    color = MockupColors.Blue
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixelIcon(iconName = "icon_trophy", size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "염창역 스타벅스 기준 가격",
                    fontSize = 14.sp,
                    color = MockupColors.TextMuted
                )
            }
        }

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

        Spacer(modifier = Modifier.weight(1f))

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
                    color = if (isPromoApplied) MockupColors.Blue else MockupColors.TextMuted
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
                                            if (result.freeDays > 0) {
                                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                                val cal = java.util.Calendar.getInstance()
                                                cal.add(java.util.Calendar.DAY_OF_MONTH, result.freeDays)
                                                preferenceManager.savePromoFreeEndDate(sdf.format(cal.time))
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
                        color = if (isPromoApplied) MockupColors.Blue else MockupColors.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Button - 실제 결제 버튼
        Button(
            onClick = {
                hapticManager?.click()
                processPayment()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPromoFree) MockupColors.Blue else MockupColors.TextPrimary
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

        // DEBUG 테스트용 건너뛰기 버튼
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
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
                    onNext()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MockupColors.TextMuted
                )
            ) {
                Text("테스트: 건너뛰기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

