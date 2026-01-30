package com.moveoftoday.walkorwait.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.PreferenceManager

/**
 * V2 펫 시스템 통합 헬퍼
 * V1 → V2 전환 및 V2 전용 기능 제공
 */
object PetSystemV2 {

    /**
     * V2 시스템 사용 여부 확인
     * V2 펫이 설정되어 있으면 V2 사용
     */
    fun isV2Enabled(preferenceManager: PreferenceManager): Boolean {
        return preferenceManager.isPetV2Initialized()
    }

    /**
     * V1 → V2 마이그레이션
     * 기존 V1 펫을 V2로 변환
     */
    fun migrateToV2(preferenceManager: PreferenceManager): Boolean {
        val v1PetTypeName = preferenceManager.getPetType() ?: return false
        val v1PetName = preferenceManager.getPetName()

        // V1 펫 타입을 V2로 매핑
        val v2PetType = mapV1ToV2PetType(v1PetTypeName)

        // V2 데이터 저장
        preferenceManager.savePetTypeV2(v2PetType)
        preferenceManager.savePetNameV2(v1PetName.ifBlank { v2PetType.displayName })

        // 기존 걸음수를 경험치로 변환
        val totalSteps = preferenceManager.getPetTotalSteps()
        val totalExp = PetLevel.stepsToExp(totalSteps.toInt())
        val level = PetLevel.levelFromExp(totalExp)

        // 레벨 1 이상으로 시작 (이미 사용 중인 유저는 알 단계 건너뜀)
        val adjustedLevel = maxOf(level, 1)
        val adjustedExp = maxOf(totalExp, PetLevel.calculateExpForLevel(1))

        preferenceManager.savePetLevelV2(
            PetLevel(
                level = adjustedLevel,
                currentExp = adjustedExp - PetLevel.calculateExpForLevel(adjustedLevel),
                totalExp = adjustedExp
            )
        )

        // 행복도 변환 (V1: 1-5 → V2: 0-100)
        val v1Happiness = preferenceManager.getPetHappiness()
        val v2Happiness = (v1Happiness * 20).coerceIn(0, 100)
        preferenceManager.savePetHappinessV2(v2Happiness)

        return true
    }

    /**
     * V1 펫 타입을 V2로 매핑
     */
    private fun mapV1ToV2PetType(v1TypeName: String): PetTypeV2 {
        return when (v1TypeName) {
            "DOG1", "DOG2" -> PetTypeV2.SHIBA
            "CAT1", "CAT2" -> PetTypeV2.CAT
            "RAT" -> PetTypeV2.HAMSTER
            "BIRD" -> PetTypeV2.PENGUIN
            else -> PetTypeV2.SHIBA
        }
    }

    /**
     * V2 펫 상태 가져오기
     */
    fun getPetState(preferenceManager: PreferenceManager): PetState? {
        val petType = preferenceManager.getPetTypeV2() ?: return null
        val petName = preferenceManager.getPetNameV2()
        val petLevel = preferenceManager.getPetLevelV2()
        val happiness = preferenceManager.getPetHappinessV2()
        val lastInteraction = preferenceManager.getPetLastInteractionTimeV2()

        return PetState(
            petType = petType,
            name = petName,
            level = petLevel,
            happiness = happiness,
            lastInteractionTime = lastInteraction
        )
    }

    /**
     * 걸음수로 경험치 추가 및 레벨업 체크
     * @return Pair(새 PetLevel, 레벨업 여부)
     */
    fun addStepsAndCheckLevelUp(
        preferenceManager: PreferenceManager,
        steps: Int
    ): Pair<PetLevel, Boolean> {
        val oldLevel = preferenceManager.getPetLevelV2()
        val newLevel = preferenceManager.addPetExpFromStepsV2(steps)
        val leveledUp = oldLevel.checkLevelUp(newLevel)
        return Pair(newLevel, leveledUp)
    }

    /**
     * 성장 단계 변화 체크
     * @return Pair(새 PetLevel, 진화 여부)
     */
    fun checkStageEvolution(
        preferenceManager: PreferenceManager,
        oldLevel: PetLevel
    ): Boolean {
        val newLevel = preferenceManager.getPetLevelV2()
        return oldLevel.checkStageEvolution(newLevel)
    }
}

/**
 * V2 펫 메인 화면 (V1과 유사한 구조)
 * PetMainScreen을 V2용으로 래핑
 */
@Composable
fun PetMainScreenV2Wrapper(
    stepCount: Int,
    goalSteps: Int,
    streakCount: Int,
    onSettingsClick: () -> Unit,
    onChallengeClick: () -> Unit = {},
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier,
    isFreeTime: Boolean = false
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    // V2 펫 상태 가져오기
    val petState = remember { PetSystemV2.getPetState(preferenceManager) }

    if (petState == null) {
        // V2 펫이 없으면 설정 화면 표시
        PetSetupFlowV2(
            onSetupComplete = {
                // 재구성을 위해 상태 갱신 필요
            },
            hapticManager = hapticManager,
            modifier = modifier
        )
        return
    }

    val progressPercent = ((stepCount.toFloat() / goalSteps) * 100).toInt().coerceAtLeast(0)
    val isWalking = progressPercent > 0 && stepCount < goalSteps
    val isGoalAchieved = stepCount >= goalSteps

    // 현재 애니메이션 타입 결정
    val animationType = petState.getCurrentAnimationType(
        isWalking = isWalking || isGoalAchieved,
        progressPercent = progressPercent,
        isNightMode = false  // TODO: 실제 야간 모드 연동
    )

    // V2 메인 화면 레이아웃 (기존 PetMainScreen과 유사)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TODO: 기존 PetMainScreen과 동일한 레이아웃 구현
        // 현재는 펫 스프라이트만 표시

        Spacer(modifier = Modifier.weight(1f))

        // V2 펫 스프라이트 with 레벨 정보
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            PetSpriteFromState(
                petState = petState,
                isWalking = isWalking || isGoalAchieved,
                progressPercent = progressPercent,
                baseSizeDp = 120,
                monochrome = true
            )
        }

        // 레벨 정보 표시
        androidx.compose.material3.Text(
            text = "Lv.${petState.level.level} ${petState.name}",
            fontSize = 16.sp,
            color = MockupColors.TextPrimary
        )

        // 성장 단계 표시
        androidx.compose.material3.Text(
            text = "${petState.stage.displayName} (${(petState.level.expProgress * 100).toInt()}%)",
            fontSize = 12.sp,
            color = MockupColors.TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * 레벨업 축하 다이얼로그
 */
@Composable
fun LevelUpCelebrationDialog(
    petState: PetState,
    oldLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null
) {
    val kenneyFont = rememberKenneyFont()
    val isStageEvolution = PetGrowthStage.fromLevel(oldLevel) != PetGrowthStage.fromLevel(newLevel)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .background(MockupColors.Background, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 펫 스프라이트 (축하 애니메이션)
            PetSpriteV2WithGlow(
                petType = petState.petType,
                stage = petState.stage,
                animationType = PetAnimationTypeV2.BARK,
                size = 100.dp,
                monochrome = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 레벨업 메시지
            androidx.compose.material3.Text(
                text = if (isStageEvolution) "진화!" else "레벨업!",
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontFamily = kenneyFont,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Text(
                text = "Lv.$oldLevel → Lv.$newLevel",
                fontSize = 18.sp,
                color = MockupColors.TextSecondary
            )

            if (isStageEvolution) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Text(
                    text = "${PetGrowthStage.fromLevel(oldLevel).displayName} → ${petState.stage.displayName}",
                    fontSize = 16.sp,
                    color = MockupColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 펫 대사
            androidx.compose.material3.Text(
                text = if (isStageEvolution) {
                    PetDialoguesV2.getEvolutionMessage(petState.petType.personality, petState.stage)
                } else {
                    PetDialoguesV2.getLevelUpMessage(petState.petType.personality, newLevel)
                },
                fontSize = 14.sp,
                color = MockupColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 확인 버튼
            androidx.compose.material3.Button(
                onClick = {
                    hapticManager?.click()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MockupColors.TextPrimary
                )
            ) {
                androidx.compose.material3.Text(
                    text = "좋아!",
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}
