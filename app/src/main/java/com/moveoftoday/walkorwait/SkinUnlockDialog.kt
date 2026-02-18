package com.moveoftoday.walkorwait

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moveoftoday.walkorwait.pet.*
import java.util.Locale

// 다국어 헬퍼
private object SkinUnlockStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun newSkinUnlocked(): String = when (getLang()) {
        "ko" -> "✧ 새 스킨 획득 ✧"
        "ja" -> "✧ 新スキン獲得 ✧"
        "zh" -> "✧ 新皮肤解锁 ✧"
        "es" -> "✧ Nueva Skin ✧"
        else -> "✧ New Skin Unlocked ✧"
    }

    fun later(): String = when (getLang()) {
        "ko" -> "나중에"
        "ja" -> "後で"
        "zh" -> "稍后"
        "es" -> "Después"
        else -> "Later"
    }

    fun equip(): String = when (getLang()) {
        "ko" -> "장착하기"
        "ja" -> "装着する"
        "zh" -> "装备"
        "es" -> "Equipar"
        else -> "Equip"
    }
}

/**
 * 스킨 해금 축하 다이얼로그
 * 새 스킨 획득 시 미리보기와 장착 옵션 제공
 */
@Composable
fun SkinUnlockDialog(
    skin: PetSkin,
    petTypeV2: PetTypeV2?,
    petStage: PetGrowthStage,
    hapticManager: HapticManager?,
    onEquip: () -> Unit,
    onLater: () -> Unit
) {
    val context = LocalContext.current

    // 등장 애니메이션
    var isVisible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scale"
    )

    // 스킨 색상 애니메이션 (반짝임 효과)
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        hapticManager?.goalAchieved()  // 축하 햅틱
    }

    Dialog(
        onDismissRequest = onLater,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .scale(scale)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이틀
            Text(
                text = SkinUnlockStrings.newSkinUnlocked(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 스킨 이름
            Text(
                text = skin.getLocalizedDisplayName(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2196F3),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 펫 미리보기 (새 스킨 적용)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Color(0xFFF5F5F5).copy(alpha = 0.8f + glowAlpha * 0.2f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (petTypeV2 != null) {
                    val previewEquipmentState = EquipmentState(
                        headId = null,
                        backgroundId = null,
                        colorId = skin.id  // 새 스킨 적용
                    )

                    PetSpriteV2WithEquipment(
                        petType = petTypeV2,
                        stage = petStage,
                        animationType = PetAnimationTypeV2.IDLE,
                        equipmentState = previewEquipmentState,
                        size = 120.dp,
                        monochrome = true,
                        showGlow = false
                    )
                } else {
                    Text(
                        text = "○",
                        fontSize = 64.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 설명
            Text(
                text = skin.description,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 나중에 버튼
                OutlinedButton(
                    onClick = {
                        hapticManager?.lightClick()
                        onLater()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text(
                        text = SkinUnlockStrings.later(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 장착하기 버튼
                Button(
                    onClick = {
                        hapticManager?.success()
                        onEquip()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text(
                        text = SkinUnlockStrings.equip(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
