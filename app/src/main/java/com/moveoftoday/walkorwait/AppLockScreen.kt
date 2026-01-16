package com.moveoftoday.walkorwait

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.moveoftoday.walkorwait.ui.theme.StandColors
import com.moveoftoday.walkorwait.ui.theme.StandTypography
import com.moveoftoday.walkorwait.ui.theme.StandSpacing
import com.moveoftoday.walkorwait.ui.theme.StandSize
import com.moveoftoday.walkorwait.ui.components.*

@Composable
fun AppLockScreen(
    preferenceManager: PreferenceManager?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // 접근성 서비스 활성화 여부 체크
    val isAccessibilityEnabled = remember {
        val enabledServices = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        enabledServices?.contains("com.moveoftoday.walkorwait") == true
    }

    // 모든 설치된 앱 가져오기 (카테고리별)
    val appsByCategory = remember {
        AppUtils.getInstalledAppsByCategory(context)
    }

    var selectedApps by remember {
        mutableStateOf(preferenceManager?.getLockedApps() ?: emptySet())
    }
    var expandedCategories by remember { mutableStateOf(setOf<AppCategory>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "제어할 앱 선택",
            fontSize = StandTypography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "목표 미달성 시\n사용이 제한될 앱을 선택하세요",
            fontSize = StandTypography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 접근성 서비스가 꺼져있을 때만 경고 표시
        if (!isAccessibilityEnabled) {
            WarningBanner(
                title = "Stand가 비활성화되어 있어요",
                description = "앱 차단 기능이 작동하지 않습니다",
                onClick = {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(bottom = StandSpacing.lg),
                icon = "⚠️"
            )
        }

        // 제한 안내
        val nextRemoveDate = preferenceManager?.getNextAppRemoveDate() ?: ""
        if (nextRemoveDate != "언제든지 가능") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StandColors.WarningLight
                )
            ) {
                Text(
                    text = "⚠️ 앱 제거: $nextRemoveDate",
                    fontSize = StandTypography.bodyMedium,
                    color = StandColors.Warning,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // 앱이 없는 경우 안내 메시지
        val totalApps = appsByCategory.values.flatten().size
        if (totalApps == 0) {
            EmptyState(
                icon = "📱",
                title = "제어할 앱이 없습니다",
                description = "Play Store에서\nYouTube, Chrome, Instagram 등\n앱을 설치해주세요",
                modifier = Modifier.weight(1f)
            )
        } else {
            // 선택된 앱 개수 표시
            if (selectedApps.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = StandColors.PrimaryLight
                    )
                ) {
                    Text(
                        text = "✓ ${selectedApps.size}개 선택됨",
                        fontSize = StandTypography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StandColors.Primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 스크롤 가능한 앱 목록
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                appsByCategory.forEach { (category, apps) ->
                    // 카테고리 헤더
                    item(key = "header_$category") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                expandedCategories = if (category in expandedCategories) {
                                    expandedCategories - category
                                } else {
                                    expandedCategories + category
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${getCategoryIcon(category)} ${category.displayName}",
                                    fontSize = StandTypography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${apps.size}개",
                                    fontSize = StandTypography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // 카테고리가 펼쳐져 있으면 앱 목록 표시
                    if (category in expandedCategories) {
                        items(
                            items = apps,
                            key = { app -> app.packageName }
                        ) { app ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    app.icon?.let {
                                        Image(
                                            bitmap = it,
                                            contentDescription = app.appName,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = app.appName,
                                        fontSize = StandTypography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Checkbox(
                                        checked = selectedApps.contains(app.packageName),
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                // 앱 추가는 자유
                                                selectedApps = selectedApps + app.packageName
                                                preferenceManager?.saveLockedApps(selectedApps)
                                            } else {
                                                // 앱 제거는 제한
                                                if (preferenceManager?.canRemoveLockedApp() == true) {
                                                    selectedApps = selectedApps - app.packageName
                                                    preferenceManager.saveLockedApps(selectedApps)
                                                    preferenceManager.saveAppRemoveTime()
                                                } else {
                                                    // Toast로 경고
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "앱 제거는 ${preferenceManager?.getNextAppRemoveDate()}에 가능해요",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedApps.isNotEmpty() || totalApps == 0
        ) {
            Text(
                text = if (totalApps == 0) "닫기" else "완료 (${selectedApps.size}개 선택)",
                fontSize = StandTypography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 카테고리별 아이콘
private fun getCategoryIcon(category: AppCategory): String {
    return when (category) {
        AppCategory.GAME -> "🎮"
        AppCategory.VIDEO -> "🎬"
        AppCategory.SOCIAL -> "💬"
        AppCategory.MUSIC_AUDIO -> "🎵"
        AppCategory.ENTERTAINMENT -> "🎪"
        AppCategory.PRODUCTIVITY -> "💼"
        AppCategory.COMMUNICATION -> "📱"
        AppCategory.SHOPPING -> "🛒"
        AppCategory.OTHER -> "📦"
    }
}