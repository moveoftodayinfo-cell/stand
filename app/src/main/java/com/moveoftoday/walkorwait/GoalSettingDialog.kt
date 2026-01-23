package com.moveoftoday.walkorwait

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.rememberKenneyFont

@Composable
fun GoalSettingDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    preferenceManager: PreferenceManager?,
    showDismissButton: Boolean = true,
    hapticManager: HapticManager? = null,
    isInitialSetup: Boolean = false
) {
    val kenneyFont = rememberKenneyFont()

    // 최소값 검증: currentGoal이 1000 미만이면 8000으로 설정
    val validGoal = if (currentGoal < 1000) 8000 else currentGoal

    // 현재 단위 설정 가져오기
    val currentUnit = preferenceManager?.getGoalUnit() ?: "steps"
    var selectedUnit by remember { mutableStateOf(currentUnit) }

    // 슬라이더 값 (걸음수 기준으로 저장)
    var goalSteps by remember { mutableFloatStateOf(validGoal.toFloat()) }

    // 범위: 걸음수 1000~55000 (100보 단위)
    val stepsRange = 1000f..55000f
    val stepsStep = 100f

    // 현재 표시값 (km는 걸음수에서 계산)
    val displayKm = goalSteps / 1300f

    // 특별 거리 라벨
    val specialLabel = when {
        selectedUnit == "km" && kotlin.math.abs(displayKm - 42.195f) < 0.2f -> "풀마라톤"
        selectedUnit == "km" && kotlin.math.abs(displayKm - 21.1f) < 0.2f -> "하프마라톤"
        else -> null
    }

    val canDecrease = preferenceManager?.canDecreaseGoal() ?: true
    val nextDecreaseDate = preferenceManager?.getNextGoalDecreaseDate() ?: ""
    val isDecrease = goalSteps.toInt() < validGoal

    // 풀스크린 스타일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 타이틀
            Text(
                text = "목표 설정",
                fontSize = 28.sp,
                fontFamily = kenneyFont,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "현재 목표: ${"%,d".format(validGoal)}보",
                fontSize = 14.sp,
                color = MockupColors.TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 단위 선택 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 걸음 수 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 3.dp,
                            color = MockupColors.Border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selectedUnit == "steps") MockupColors.Border else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            hapticManager?.click()
                            selectedUnit = "steps"
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "걸음 수",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUnit == "steps") Color.White else MockupColors.TextSecondary
                    )
                }

                // km 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 3.dp,
                            color = MockupColors.Border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selectedUnit == "km") MockupColors.Border else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            hapticManager?.click()
                            selectedUnit = "km"
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "거리 (km)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUnit == "km") Color.White else MockupColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 현재 값 표시 영역 (높이 고정)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedUnit == "km") {
                            if (kotlin.math.abs(displayKm - 42.195f) < 0.2f) "42.195 km"
                            else if (kotlin.math.abs(displayKm - 21.1f) < 0.2f) "21.1 km"
                            else String.format("%.1f km", displayKm)
                        } else {
                            "%,d보".format(goalSteps.toInt())
                        },
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupColors.TextPrimary
                    )

                    // 특별 라벨 영역 (높이 고정)
                    Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.Center) {
                        if (specialLabel != null) {
                            Text(
                                text = specialLabel,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    // 환산 값 표시
                    Text(
                        text = if (selectedUnit == "km") {
                            "약 %,d보".format(goalSteps.toInt())
                        } else {
                            "약 ${String.format("%.1f", displayKm)}km"
                        },
                        fontSize = 14.sp,
                        color = MockupColors.TextMuted
                    )
                }
            }

            // km 모드일 때 퀵 선택 버튼 (높이 고정 영역)
            Box(modifier = Modifier.height(40.dp)) {
                if (selectedUnit == "km") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 하프마라톤 버튼
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable {
                                    hapticManager?.lightClick()
                                    goalSteps = 21.1f * 1300f // 약 27430보
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "하프 21.1km",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                        }

                        // 풀마라톤 버튼
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, MockupColors.Border, RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .clickable {
                                    hapticManager?.lightClick()
                                    goalSteps = 42.195f * 1300f // 약 54854보
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "풀 42.195km",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 슬라이더
            Slider(
                value = goalSteps,
                onValueChange = { newValue ->
                    // 100보 단위로 반올림
                    val roundedValue = (newValue / stepsStep).toInt() * stepsStep
                    goalSteps = roundedValue.coerceIn(stepsRange.start, stepsRange.endInclusive)
                    hapticManager?.lightClick()
                },
                valueRange = stepsRange,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MockupColors.Border,
                    activeTrackColor = MockupColors.Border,
                    inactiveTrackColor = Color(0xFFE0E0E0)
                )
            )

            // 범위 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedUnit == "km") "0.8km" else "1,000보",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
                Text(
                    text = if (selectedUnit == "km") "42.3km" else "55,000보",
                    fontSize = 12.sp,
                    color = MockupColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 목표 감소 불가 경고
            if (isDecrease && !canDecrease) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ 목표 감소는 $nextDecreaseDate 부터 가능합니다",
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showDismissButton) {
                    // 취소 버튼
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable {
                                hapticManager?.click()
                                onDismiss()
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )
                    }
                }

                // 확인 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                        .background(
                            if (isDecrease && !canDecrease) MockupColors.TextMuted else MockupColors.Border,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !isDecrease || canDecrease) {
                            hapticManager?.success()
                            if (isDecrease && !isInitialSetup) {
                                preferenceManager?.saveGoalDecreaseTime()
                            }
                            preferenceManager?.saveGoalUnit(selectedUnit)
                            onConfirm(goalSteps.toInt())
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = kenneyFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
