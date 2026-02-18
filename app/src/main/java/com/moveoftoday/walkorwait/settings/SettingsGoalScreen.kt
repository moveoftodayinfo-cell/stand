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
import java.util.Locale

// ============ 다국어 지원 헬퍼 객체 ============
private object SettingsGoalStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun dailyGoal(): String = when (getLang()) {
        "ko" -> "일일 목표"
        "ja" -> "1日の目標"
        "zh" -> "每日目标"
        "es" -> "Meta diaria"
        else -> "Daily Goal"
    }

    fun stepGoal(): String = when (getLang()) {
        "ko" -> "걸음 목표"
        "ja" -> "歩数目標"
        "zh" -> "步数目标"
        "es" -> "Meta de pasos"
        else -> "Step Goal"
    }

    fun controlDays(): String = when (getLang()) {
        "ko" -> "제어 요일"
        "ja" -> "制御曜日"
        "zh" -> "控制星期"
        "es" -> "Días de control"
        else -> "Control Days"
    }

    fun selectDays(): String = when (getLang()) {
        "ko" -> "요일 선택"
        "ja" -> "曜日選択"
        "zh" -> "选择星期"
        "es" -> "Seleccionar días"
        else -> "Select Days"
    }

    fun everyday(): String = when (getLang()) {
        "ko" -> "매일"
        "ja" -> "毎日"
        "zh" -> "每天"
        "es" -> "Todos los días"
        else -> "Everyday"
    }

    fun blockingPeriods(): String = when (getLang()) {
        "ko" -> "차단 시간대"
        "ja" -> "ブロック時間帯"
        "zh" -> "阻止时段"
        "es" -> "Periodos de bloqueo"
        else -> "Blocking Periods"
    }

    fun selectPeriods(): String = when (getLang()) {
        "ko" -> "시간대 선택"
        "ja" -> "時間帯選択"
        "zh" -> "选择时段"
        "es" -> "Seleccionar periodos"
        else -> "Select Periods"
    }

    fun notSet(): String = when (getLang()) {
        "ko" -> "설정 안 함"
        "ja" -> "設定なし"
        "zh" -> "未设置"
        "es" -> "No configurado"
        else -> "Not Set"
    }

    fun periodsCount(count: Int): String = when (getLang()) {
        "ko" -> "${count}개 시간대"
        "ja" -> "${count}個の時間帯"
        "zh" -> "${count}个时段"
        "es" -> "$count periodos"
        else -> "$count periods"
    }

    fun stepsUnit(): String = when (getLang()) {
        "ko" -> "보"
        "ja" -> "歩"
        "zh" -> "步"
        "es" -> " pasos"
        else -> " steps"
    }

    fun minutesUnit(): String = when (getLang()) {
        "ko" -> "분"
        "ja" -> "分"
        "zh" -> "分钟"
        "es" -> " min"
        else -> " min"
    }

    fun example(): String = when (getLang()) {
        "ko" -> "예시:"
        "ja" -> "例:"
        "zh" -> "示例:"
        "es" -> "Ejemplo:"
        else -> "Example:"
    }

    fun morning(): String = when (getLang()) {
        "ko" -> "오전 (06:00 ~ 12:00)"
        "ja" -> "午前 (06:00 ~ 12:00)"
        "zh" -> "上午 (06:00 ~ 12:00)"
        "es" -> "Mañana (06:00 ~ 12:00)"
        else -> "Morning (06:00 ~ 12:00)"
    }

    fun afternoon(): String = when (getLang()) {
        "ko" -> "오후 (12:00 ~ 18:00)"
        "ja" -> "午後 (12:00 ~ 18:00)"
        "zh" -> "下午 (12:00 ~ 18:00)"
        "es" -> "Tarde (12:00 ~ 18:00)"
        else -> "Afternoon (12:00 ~ 18:00)"
    }

    fun evening(): String = when (getLang()) {
        "ko" -> "저녁 (18:00 ~ 22:00)"
        "ja" -> "夕方 (18:00 ~ 22:00)"
        "zh" -> "傍晚 (18:00 ~ 22:00)"
        "es" -> "Noche (18:00 ~ 22:00)"
        else -> "Evening (18:00 ~ 22:00)"
    }

    fun night(): String = when (getLang()) {
        "ko" -> "심야 (22:00 ~ 06:00)"
        "ja" -> "深夜 (22:00 ~ 06:00)"
        "zh" -> "深夜 (22:00 ~ 06:00)"
        "es" -> "Madrugada (22:00 ~ 06:00)"
        else -> "Night (22:00 ~ 06:00)"
    }

    fun exampleMorning(): String = when (getLang()) {
        "ko" -> "09:00 ~ 12:00 (오전)"
        "ja" -> "09:00 ~ 12:00 (午前)"
        "zh" -> "09:00 ~ 12:00 (上午)"
        "es" -> "09:00 ~ 12:00 (Mañana)"
        else -> "09:00 ~ 12:00 (Morning)"
    }

    fun exampleAfternoon(): String = when (getLang()) {
        "ko" -> "14:00 ~ 18:00 (오후)"
        "ja" -> "14:00 ~ 18:00 (午後)"
        "zh" -> "14:00 ~ 18:00 (下午)"
        "es" -> "14:00 ~ 18:00 (Tarde)"
        else -> "14:00 ~ 18:00 (Afternoon)"
    }

    fun getDayNames(): List<String> = when (getLang()) {
        "ko" -> listOf("일", "월", "화", "수", "목", "금", "토")
        "ja" -> listOf("日", "月", "火", "水", "木", "金", "土")
        "zh" -> listOf("日", "一", "二", "三", "四", "五", "六")
        "es" -> listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
        else -> listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }

    fun getPeriodDisplayText(period: String): String = when (period) {
        "morning" -> morning()
        "afternoon" -> afternoon()
        "evening" -> evening()
        "night" -> night()
        else -> period
    }
}

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
                RetroSectionTitle(SettingsGoalStrings.dailyGoal(), kenneyFont)

                val goalText = when (goalUnit) {
                    "km" -> "${goal / 1000.0}km"
                    "minutes" -> "${goal}${SettingsGoalStrings.minutesUnit()}"
                    else -> "${goal}${SettingsGoalStrings.stepsUnit()}"
                }

                SettingsItemCard(
                    title = SettingsGoalStrings.stepGoal(),
                    value = goalText,
                    kenneyFont = kenneyFont,
                    onClick = {
                        hapticManager.click()
                        showGoalDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ========== 제어 요일 ==========
                RetroSectionTitle(SettingsGoalStrings.controlDays(), kenneyFont)

                val dayNames = SettingsGoalStrings.getDayNames()
                val daysText = if (controlDays.isEmpty()) {
                    SettingsGoalStrings.everyday()
                } else {
                    controlDays.sorted().map { dayNames[it % 7] }.joinToString(", ")
                }

                SettingsItemCard(
                    title = SettingsGoalStrings.selectDays(),
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
                RetroSectionTitle(SettingsGoalStrings.blockingPeriods(), kenneyFont)

                val periodsText = if (blockingPeriods.isEmpty()) {
                    SettingsGoalStrings.notSet()
                } else {
                    SettingsGoalStrings.periodsCount(blockingPeriods.size)
                }

                SettingsItemCard(
                    title = SettingsGoalStrings.selectPeriods(),
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
                                    val displayText = SettingsGoalStrings.getPeriodDisplayText(period)
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
                                text = SettingsGoalStrings.example(),
                                fontSize = 12.sp,
                                color = MockupColors.TextMuted
                            )
                            Text(
                                text = SettingsGoalStrings.exampleMorning(),
                                fontSize = 14.sp,
                                color = MockupColors.TextMuted
                            )
                            Text(
                                text = SettingsGoalStrings.exampleAfternoon(),
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
