package com.moveoftoday.walkorwait

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*

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
    // 최소값 검증: currentGoal이 1000 미만이면 8000으로 설정
    val validGoal = if (currentGoal < 1000) 8000 else currentGoal

    // 현재 단위 설정 가져오기
    val currentUnit = preferenceManager?.getGoalUnit() ?: "steps"
    var selectedUnit by remember { mutableStateOf(currentUnit) }

    // 단위에 따라 초기값 설정
    val initialValue = if (selectedUnit == "km") {
        String.format("%.2f", validGoal / 1300.0)
    } else {
        validGoal.toString()
    }
    var inputGoal by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf("") }

    val canDecrease = preferenceManager?.canDecreaseGoal() ?: false
    val nextDecreaseDate = preferenceManager?.getNextGoalDecreaseDate() ?: ""

    // 현재 입력값을 걸음수로 환산해서 비교
    val currentInputAsSteps = if (selectedUnit == "km") {
        inputGoal.toDoubleOrNull()?.times(1300)?.toInt() ?: 0
    } else {
        inputGoal.toIntOrNull() ?: 0
    }
    val isDecrease = currentInputAsSteps < validGoal

    Dialog(onDismissRequest = { if (showDismissButton) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = StandColors.DarkBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(StandSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "목표 설정",
                    fontSize = StandTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(StandSpacing.lg))

                // 현재 목표 표시 (단위에 맞게)
                val currentGoalDisplay = if (selectedUnit == "km") {
                    String.format("%.2f km", validGoal / 1300.0)
                } else {
                    "$validGoal 걸음"
                }
                Text(
                    text = if (currentGoal < 1000) "권장 목표: 8000 걸음" else "현재 목표: $currentGoalDisplay",
                    fontSize = StandTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(StandSpacing.lg))

                // 단위 선택 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StandSpacing.sm)
                ) {
                    // 걸음 수 버튼
                    Button(
                        onClick = {
                            hapticManager?.click()
                            if (selectedUnit != "steps") {
                                // km -> 걸음수로 변환
                                val kmValue = inputGoal.toDoubleOrNull() ?: 0.0
                                val stepsValue = (kmValue * 1300).toInt()
                                inputGoal = stepsValue.toString()
                                selectedUnit = "steps"
                                errorMessage = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedUnit == "steps") StandColors.Primary else Color.White.copy(alpha = 0.1f),
                            contentColor = if (selectedUnit == "steps") Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("걸음 수")
                    }

                    // km 버튼
                    Button(
                        onClick = {
                            hapticManager?.click()
                            if (selectedUnit != "km") {
                                // 걸음수 -> km로 변환
                                val stepsValue = inputGoal.toIntOrNull() ?: 0
                                val kmValue = stepsValue / 1300.0
                                inputGoal = String.format("%.2f", kmValue)
                                selectedUnit = "km"
                                errorMessage = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedUnit == "km") StandColors.Primary else Color.White.copy(alpha = 0.1f),
                            contentColor = if (selectedUnit == "km") Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("거리 (km)")
                    }
                }

                Spacer(modifier = Modifier.height(StandSpacing.lg))

                // 단위에 따른 입력 필드
                if (selectedUnit == "km") {
                    OutlinedTextField(
                        value = inputGoal,
                        onValueChange = { newValue ->
                            // 소수점 포함 숫자만 허용
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                inputGoal = newValue

                                val goal = newValue.toDoubleOrNull()
                                errorMessage = when {
                                    goal == null && newValue.isNotEmpty() && newValue != "." -> "숫자만 입력하세요"
                                    goal != null && goal < 0.8 -> "최소 0.8km 이상 설정하세요"
                                    goal != null && goal > 40.0 -> "최대 40km까지 가능합니다"
                                    else -> ""
                                }
                            }
                        },
                        label = { Text("목표 거리 (km)", color = Color.White.copy(alpha = 0.7f)) },
                        isError = errorMessage.isNotEmpty(),
                        supportingText = {
                            if (errorMessage.isNotEmpty()) {
                                Text(errorMessage, color = Color.Red)
                            } else {
                                Text("0.8km ~ 40km까지 가능합니다", color = Color.White.copy(alpha = 0.5f))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = StandColors.Primary,
                            focusedBorderColor = StandColors.Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = StandColors.Primary,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = inputGoal,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                inputGoal = newValue

                                val goal = newValue.toIntOrNull()
                                errorMessage = when {
                                    goal == null && newValue.isNotEmpty() -> "숫자만 입력하세요"
                                    goal != null && goal < 1000 -> "최소 1000보 이상 설정하세요"
                                    goal != null && goal > 50000 -> "최대 50000보까지 가능합니다"
                                    else -> ""
                                }
                            }
                        },
                        label = { Text("목표 걸음 수", color = Color.White.copy(alpha = 0.7f)) },
                        isError = errorMessage.isNotEmpty(),
                        supportingText = {
                            if (errorMessage.isNotEmpty()) {
                                Text(errorMessage, color = Color.Red)
                            } else {
                                Text("최대 50000보까지 가능합니다", color = Color.White.copy(alpha = 0.5f))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = StandColors.Primary,
                            focusedBorderColor = StandColors.Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedLabelColor = StandColors.Primary,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                if (isDecrease && !canDecrease) {
                    Spacer(modifier = Modifier.height(StandSpacing.sm))
                    StatusCard(statusType = StatusType.WARNING) {
                        Text(
                            text = "⚠️ 목표 감소는 $nextDecreaseDate 가능합니다",
                            fontSize = StandTypography.labelLarge,
                            color = StandColors.Warning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(StandSpacing.xxl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (showDismissButton) Arrangement.SpaceBetween else Arrangement.Center
                ) {
                    if (showDismissButton) {
                        TextButton(onClick = {
                            hapticManager?.click()
                            onDismiss()
                        }) {
                            Text("취소")
                        }
                    }

                    Button(
                        onClick = {
                            if (isDecrease && !canDecrease) {
                                // 감소 불가
                                hapticManager?.warning()
                            } else {
                                hapticManager?.success()
                                if (isDecrease && !isInitialSetup) {
                                    preferenceManager?.saveGoalDecreaseTime()
                                }
                                // 단위 저장
                                preferenceManager?.saveGoalUnit(selectedUnit)

                                // km인 경우 걸음수로 변환해서 저장
                                val goalInSteps = if (selectedUnit == "km") {
                                    (inputGoal.toDoubleOrNull()?.times(1300))?.toInt() ?: 8000
                                } else {
                                    inputGoal.toIntOrNull() ?: 8000
                                }
                                onConfirm(goalInSteps)
                            }
                        },
                        enabled = errorMessage.isEmpty() && (
                            if (selectedUnit == "km") {
                                inputGoal.isNotEmpty() && inputGoal.toDoubleOrNull() != null
                            } else {
                                inputGoal.isNotEmpty() && inputGoal.toIntOrNull() != null
                            }
                        ) && (!isDecrease || canDecrease)
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}