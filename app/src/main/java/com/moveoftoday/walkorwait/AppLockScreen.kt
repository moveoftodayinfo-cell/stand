package com.moveoftoday.walkorwait

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.rememberKenneyFont
import java.util.Locale

// 다국어 헬퍼
private object AppLockStrings {
    private fun getLang(): String = Locale.getDefault().language

    fun selectApps(): String = when (getLang()) {
        "ko" -> "제어할 앱 선택"
        "ja" -> "制御するアプリを選択"
        "zh" -> "选择要控制的应用"
        "es" -> "Seleccionar apps"
        else -> "Select Apps to Control"
    }

    fun selectAppsDesc(): String = when (getLang()) {
        "ko" -> "목표 미달성 시\n사용이 제한될 앱을 선택하세요"
        "ja" -> "目標未達成時に\n使用が制限されるアプリを選択"
        "zh" -> "目标未达成时\n将限制使用的应用"
        "es" -> "Apps limitadas si\nno alcanzas tu meta"
        else -> "Apps will be blocked\nif you don't reach your goal"
    }

    fun rebonDisabled(): String = when (getLang()) {
        "ko" -> "rebon이 비활성화되어 있어요"
        "ja" -> "rebonが無効になっています"
        "zh" -> "rebon已禁用"
        "es" -> "rebon está desactivado"
        else -> "rebon is disabled"
    }

    fun blockingNotWorking(): String = when (getLang()) {
        "ko" -> "앱 차단 기능이 작동하지 않습니다"
        "ja" -> "アプリブロック機能が動作しません"
        "zh" -> "应用阻止功能无法工作"
        "es" -> "El bloqueo no funciona"
        else -> "App blocking won't work"
    }

    fun noApps(): String = when (getLang()) {
        "ko" -> "제어할 앱이 없습니다"
        "ja" -> "制御するアプリがありません"
        "zh" -> "没有可控制的应用"
        "es" -> "No hay apps"
        else -> "No apps available"
    }

    fun installApps(): String = when (getLang()) {
        "ko" -> "Play Store에서\nYouTube, Chrome, Instagram 등\n앱을 설치해주세요"
        "ja" -> "Play Storeから\nYouTube、Chrome、Instagramなど\nアプリをインストールしてください"
        "zh" -> "请从Play Store安装\nYouTube、Chrome、Instagram等应用"
        "es" -> "Instala apps desde\nPlay Store como\nYouTube, Chrome, Instagram"
        else -> "Install apps from\nPlay Store like\nYouTube, Chrome, Instagram"
    }

    fun selected(count: Int): String = when (getLang()) {
        "ko" -> "${count}개 선택됨"
        "ja" -> "${count}個選択中"
        "zh" -> "已选${count}个"
        "es" -> "$count seleccionado(s)"
        else -> "$count selected"
    }

    fun appCount(count: Int): String = when (getLang()) {
        "ko" -> "${count}개"
        "ja" -> "${count}個"
        "zh" -> "${count}个"
        else -> "$count"
    }

    fun removeAvailableFrom(date: String): String = when (getLang()) {
        "ko" -> "앱 제거는 ${date}에 가능해요"
        "ja" -> "アプリの削除は${date}から可能です"
        "zh" -> "可在${date}后移除应用"
        "es" -> "Eliminar disponible desde $date"
        else -> "Remove available from $date"
    }

    fun close(): String = when (getLang()) {
        "ko" -> "닫기"
        "ja" -> "閉じる"
        "zh" -> "关闭"
        "es" -> "Cerrar"
        else -> "Close"
    }

    fun doneWithCount(count: Int): String = when (getLang()) {
        "ko" -> "완료 (${count}개 선택)"
        "ja" -> "完了 (${count}個選択)"
        "zh" -> "完成 (已选${count}个)"
        "es" -> "Listo ($count)"
        else -> "Done ($count selected)"
    }
}

@Composable
fun AppLockScreen(
    preferenceManager: PreferenceManager?,
    onBack: () -> Unit,
    onLockedAppsChanged: ((Set<String>) -> Unit)? = null
) {
    val context = LocalContext.current
    val kenneyFont = rememberKenneyFont()
    val hapticManager = remember { HapticManager(context) }

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

    // 튜토리얼과 동일한 화이트 레트로 스타일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(28.dp))

            // 타이틀
            Text(
                text = AppLockStrings.selectApps(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MockupColors.TextPrimary,
                fontFamily = kenneyFont
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppLockStrings.selectAppsDesc(),
                fontSize = 14.sp,
                color = MockupColors.TextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 접근성 서비스가 꺼져있을 때 경고 표시
            if (!isAccessibilityEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MockupColors.Red, RoundedCornerShape(12.dp))
                        .background(MockupColors.RedLight, RoundedCornerShape(12.dp))
                        .clickable {
                            hapticManager.warning()
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                            )
                            context.startActivity(intent)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelIcon(iconName = "icon_dialog", size = 28.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppLockStrings.rebonDisabled(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MockupColors.Red
                            )
                            Text(
                                text = AppLockStrings.blockingNotWorking(),
                                fontSize = 13.sp,
                                color = MockupColors.TextSecondary
                            )
                        }
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            color = MockupColors.TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 앱이 없는 경우 안내 메시지
            val totalApps = appsByCategory.values.flatten().size
            if (totalApps == 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PixelIcon(iconName = "icon_lock", size = 64.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = AppLockStrings.noApps(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.TextPrimary,
                            fontFamily = kenneyFont
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppLockStrings.installApps(),
                            fontSize = 14.sp,
                            color = MockupColors.TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                // 선택된 앱 개수 표시
                if (selectedApps.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MockupColors.Blue, RoundedCornerShape(8.dp))
                            .background(MockupColors.BlueLight, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = AppLockStrings.selected(selectedApps.size),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupColors.Blue
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 스크롤 가능한 앱 목록
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    appsByCategory.forEach { (category, apps) ->
                        // 카테고리 헤더
                        item(key = "header_$category") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    hapticManager.lightClick()
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
                                        text = AppLockStrings.appCount(apps.size),
                                        fontSize = 13.sp,
                                        color = MockupColors.TextMuted
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp)
                                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                        .clickable {
                                            hapticManager.lightClick()
                                            val isSelected = selectedApps.contains(app.packageName)
                                            if (!isSelected) {
                                                selectedApps = selectedApps + app.packageName
                                                preferenceManager?.saveLockedApps(selectedApps)
                                                onLockedAppsChanged?.invoke(selectedApps)
                                            } else {
                                                if (preferenceManager?.canRemoveLockedApp() == true) {
                                                    selectedApps = selectedApps - app.packageName
                                                    preferenceManager.saveLockedApps(selectedApps)
                                                    onLockedAppsChanged?.invoke(selectedApps)
                                                    preferenceManager.saveAppRemoveTime()
                                                } else {
                                                    hapticManager.warning()
                                                    android.widget.Toast
                                                        .makeText(
                                                            context,
                                                            AppLockStrings.removeAvailableFrom(preferenceManager?.getNextAppRemoveDate() ?: ""),
                                                            android.widget.Toast.LENGTH_LONG
                                                        )
                                                        .show()
                                                }
                                            }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    app.icon?.let {
                                        Image(
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
                                            hapticManager.lightClick()
                                            if (checked) {
                                                selectedApps = selectedApps + app.packageName
                                                preferenceManager?.saveLockedApps(selectedApps)
                                                onLockedAppsChanged?.invoke(selectedApps)
                                            } else {
                                                if (preferenceManager?.canRemoveLockedApp() == true) {
                                                    selectedApps = selectedApps - app.packageName
                                                    preferenceManager.saveLockedApps(selectedApps)
                                                    onLockedAppsChanged?.invoke(selectedApps)
                                                    preferenceManager.saveAppRemoveTime()
                                                } else {
                                                    hapticManager.warning()
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        AppLockStrings.removeAvailableFrom(preferenceManager?.getNextAppRemoveDate() ?: ""),
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                }
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

                    // 하단 여백
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 완료 버튼 - 튜토리얼과 동일한 스타일
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, MockupColors.Border, RoundedCornerShape(12.dp))
                    .background(MockupColors.Border, RoundedCornerShape(12.dp))
                    .clickable {
                        hapticManager.success()
                        onBack()
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (totalApps == 0) AppLockStrings.close() else AppLockStrings.doneWithCount(selectedApps.size),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = kenneyFont
                )
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// 카테고리별 표시 (이모지 제거)
private fun getCategoryIcon(category: AppCategory): String {
    return ""
}
