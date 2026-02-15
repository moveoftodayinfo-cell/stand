package com.moveoftoday.walkorwait.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.*
import com.moveoftoday.walkorwait.pet.*

/**
 * 목표 설정 화면 (걸음 목표, 요일, 시간대)
 */
@Composable
fun SettingsGoalScreen(
    preferenceManager: PreferenceManager?,
    repository: UserDataRepository,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()

    // 상태
    var goal by remember { mutableIntStateOf(repository.getGoal()) }
    var goalUnit by remember { mutableStateOf(preferenceManager?.getGoalUnit() ?: "steps") }
    var controlDays by remember { mutableStateOf(preferenceManager?.getControlDays() ?: emptySet<Int>()) }
    var blockingPeriods by remember { mutableStateOf(preferenceManager?.getBlockingPeriods() ?: emptySet<String>()) }

    // 다이얼로그 상태
    var showGoalDialog by remember { mutableStateOf(false) }
    var showControlDaysDialog by remember { mutableStateOf(false) }
    var showBlockingPeriodsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            SettingsHeader(
                title = "goal",
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
                // ========== 일일 목표 ==========
                RetroSectionTitle("일일 목표", kenneyFont)

                val goalText = when (goalUnit) {
                    "km" -> "${goal / 1000.0}km"
                    "minutes" -> "${goal}분"
                    else -> "${goal}보"
                }

                SettingsItemCard(
                    title = "걸음 목표",
                    value = goalText,
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        showGoalDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 제어 요일 ==========
                RetroSectionTitle("제어 요일", kenneyFont)

                val dayNames = listOf("일", "월", "화", "수", "목", "금", "토")
                val daysText = if (controlDays.isEmpty()) {
                    "매일"
                } else {
                    controlDays.sorted().map { dayNames[it % 7] }.joinToString(", ")
                }

                SettingsItemCard(
                    title = "요일 선택",
                    value = daysText,
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        showControlDaysDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 요일 칩 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayNames.forEachIndexed { index, name ->
                        val isSelected = controlDays.isEmpty() || controlDays.contains(index)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(
                                    2.dp,
                                    if (isSelected) MockupColors.TextPrimary else MockupColors.Border,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isSelected) MockupColors.Border.copy(alpha = 0.2f) else MockupColors.CardBackground,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MockupColors.TextPrimary else MockupColors.TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 차단 시간대 ==========
                RetroSectionTitle("차단 시간대", kenneyFont)

                val periodsText = if (blockingPeriods.isEmpty()) {
                    "설정 안 함"
                } else {
                    "${blockingPeriods.size}개 시간대"
                }

                SettingsItemCard(
                    title = "시간대 선택",
                    value = periodsText,
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        showBlockingPeriodsDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 시간대 표시 (비어있으면 예시 표시)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                        .background(MockupColors.CardBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    if (blockingPeriods.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // 정렬 순서: morning → afternoon → evening → night
                            val sortOrder = listOf("morning", "afternoon", "evening", "night")
                            blockingPeriods
                                .sortedBy { sortOrder.indexOf(it).takeIf { i -> i >= 0 } ?: 99 }
                                .forEach { period ->
                                    val displayText = when (period) {
                                        "morning" -> "오전 (06:00 ~ 12:00)"
                                        "afternoon" -> "오후 (12:00 ~ 18:00)"
                                        "evening" -> "저녁 (18:00 ~ 22:00)"
                                        "night" -> "심야 (22:00 ~ 06:00)"
                                        else -> period
                                    }
                                    Text(
                                        text = displayText,
                                        fontSize = 14.sp,
                                        color = MockupColors.TextPrimary
                                    )
                                }
                        }
                    } else {
                        // 예시 표시
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "예시:",
                                fontSize = 12.sp,
                                color = MockupColors.TextMuted
                            )
                            Text(
                                text = "09:00 ~ 12:00 (오전)",
                                fontSize = 14.sp,
                                color = MockupColors.TextMuted
                            )
                            Text(
                                text = "14:00 ~ 18:00 (오후)",
                                fontSize = 14.sp,
                                color = MockupColors.TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // 목표 설정 다이얼로그
        if (showGoalDialog) {
            GoalSettingDialog(
                currentGoal = goal,
                onDismiss = { showGoalDialog = false },
                onConfirm = { newGoal ->
                    repository.saveGoal(newGoal)
                    goal = newGoal
                    goalUnit = preferenceManager?.getGoalUnit() ?: "steps"
                    showGoalDialog = false
                    StepWidgetProvider.updateAllWidgets(context)
                },
                preferenceManager = preferenceManager,
                hapticManager = hapticManager
            )
        }

        // 제어 요일 다이얼로그
        if (showControlDaysDialog) {
            val canRemoveDays = preferenceManager?.canChangeControlDays() ?: true
            ControlDaysDialog(
                currentDays = controlDays,
                canRemove = canRemoveDays,
                nextRemoveDate = if (!canRemoveDays) preferenceManager?.getNextControlDaysChangeDate() ?: "" else "",
                onDismiss = { showControlDaysDialog = false },
                onConfirm = { newDays, hasRemovals ->
                    preferenceManager?.saveControlDays(newDays)
                    controlDays = newDays
                    if (hasRemovals) {
                        preferenceManager?.saveControlDaysChangeTime()
                    }
                    showControlDaysDialog = false
                }
            )
        }

        // 차단 시간대 다이얼로그
        if (showBlockingPeriodsDialog) {
            val canRemovePeriods = preferenceManager?.canChangeBlockingPeriods() ?: true
            BlockingPeriodsDialog(
                currentPeriods = blockingPeriods,
                canRemove = canRemovePeriods,
                nextRemoveDate = if (!canRemovePeriods) preferenceManager?.getNextBlockingPeriodsChangeDate() ?: "" else "",
                onDismiss = { showBlockingPeriodsDialog = false },
                onConfirm = { newPeriods, hasRemovals ->
                    preferenceManager?.saveBlockingPeriods(newPeriods)
                    blockingPeriods = newPeriods
                    if (hasRemovals) {
                        preferenceManager?.saveBlockingPeriodsChangeTime()
                    }
                    showBlockingPeriodsDialog = false
                }
            )
        }
    }
}

@Composable
private fun SettingsItemCard(
    title: String,
    value: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MockupColors.TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MockupColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kenneyFont
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ">",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupColors.Border,
                    fontFamily = kenneyFont
                )
            }
        }
    }
}
