package com.moveoftoday.walkorwait.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize

/**
 * Stand 앱 공통 컴포넌트 모음
 */

// ============================================
// Warning Banner (경고 배너)
// ============================================
/**
 * 경고 배너 컴포넌트
 * 접근성 서비스 비활성화 등 경고 표시에 사용
 */
@Composable
fun WarningBanner(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String = "⚠️"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = StandColors.ErrorMedium
        ),
        shape = RoundedCornerShape(StandSize.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(StandSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = StandTypography.headlineSmall
            )
            Spacer(modifier = Modifier.width(StandSpacing.iconGapLarge))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = StandTypography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = StandColors.Error
                )
                Spacer(modifier = Modifier.height(StandSpacing.textGap))
                Text(
                    text = description,
                    fontSize = StandTypography.bodySmall,
                    color = StandColors.TextSecondary,
                    lineHeight = StandTypography.titleSmall
                )
            }
            Text(
                text = "→",
                fontSize = StandTypography.headlineSmall,
                color = StandColors.Error
            )
        }
    }
}

// ============================================
// Status Card (상태 카드)
// ============================================
/**
 * 상태 표시 카드
 * 성공/경고/에러 상태에 따라 색상 변경
 */
enum class StatusType {
    SUCCESS, WARNING, ERROR, PRIMARY
}

@Composable
fun StatusCard(
    statusType: StatusType,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = when (statusType) {
        StatusType.SUCCESS -> StandColors.SuccessLight
        StatusType.WARNING -> StandColors.WarningLight
        StatusType.ERROR -> StandColors.ErrorLight
        StatusType.PRIMARY -> StandColors.PrimaryLight
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(StandSize.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(StandSpacing.cardPadding),
            content = content
        )
    }
}

// ============================================
// Primary Button (주요 버튼)
// ============================================
/**
 * 주요 액션 버튼
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(StandSize.buttonHeight),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = StandColors.Primary,
            disabledContainerColor = StandColors.Primary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(StandSize.cardCornerRadius)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 위험/취소 액션 버튼
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(StandSize.buttonHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = StandColors.Error
        ),
        shape = RoundedCornerShape(StandSize.cardCornerRadius)
    ) {
        Text(
            text = text,
            fontSize = StandTypography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 보조 버튼 (Outlined)
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(StandSize.buttonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(StandSize.cardCornerRadius)
    ) {
        Text(
            text = text,
            fontSize = StandTypography.bodyLarge,
            color = StandColors.Primary
        )
    }
}

// ============================================
// Section Header (섹션 헤더)
// ============================================
/**
 * 섹션 제목 헤더
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = StandTypography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(StandSpacing.textGap))
            Text(
                text = subtitle,
                fontSize = StandTypography.bodyMedium,
                color = StandColors.TextSecondary
            )
        }
    }
}

// ============================================
// Info Row (정보 행)
// ============================================
/**
 * 라벨-값 형태의 정보 표시 행
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = StandTypography.bodyMedium,
            color = StandColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = StandTypography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

// ============================================
// Progress Card (진행률 카드)
// ============================================
/**
 * 진행률 표시 카드
 */
@Composable
fun ProgressCard(
    title: String,
    currentValue: String,
    progress: Float,
    statusType: StatusType,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val progressColor = when (statusType) {
        StatusType.SUCCESS -> StandColors.Success
        StatusType.WARNING -> StandColors.Warning
        StatusType.ERROR -> StandColors.Error
        StatusType.PRIMARY -> StandColors.Primary
    }

    StatusCard(statusType = statusType, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = StandTypography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentValue,
                fontSize = StandTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }
        Spacer(modifier = Modifier.height(StandSpacing.itemGap))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(StandSize.progressBarHeight),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f)
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(StandSpacing.itemGap))
            Text(
                text = subtitle,
                fontSize = StandTypography.bodySmall,
                color = StandColors.TextSecondary
            )
        }
    }
}

// ============================================
// Empty State (빈 상태)
// ============================================
/**
 * 빈 상태 표시 컴포넌트
 */
@Composable
fun EmptyState(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(StandSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = StandTypography.displayLarge
        )
        Spacer(modifier = Modifier.height(StandSpacing.lg))
        Text(
            text = title,
            fontSize = StandTypography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(StandSpacing.sm))
        Text(
            text = description,
            fontSize = StandTypography.bodyMedium,
            color = StandColors.TextSecondary
        )
    }
}

// ============================================
// Setting Item (설정 아이템)
// ============================================
/**
 * 설정 화면의 개별 설정 아이템
 */
@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = StandColors.CardBackground
        ),
        shape = RoundedCornerShape(StandSize.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(StandSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = StandTypography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(StandSpacing.textGap))
                    Text(
                        text = subtitle,
                        fontSize = StandTypography.bodySmall,
                        color = StandColors.TextSecondary
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Text(
                    text = "→",
                    fontSize = StandTypography.titleMedium,
                    color = StandColors.TextSecondary
                )
            }
        }
    }
}
